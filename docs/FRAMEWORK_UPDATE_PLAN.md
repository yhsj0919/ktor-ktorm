# 框架更新计划

本文档用于记录当前 `ktor-ktorm` 骨架在对标 RuoYi 一类后台框架前，需要优先完成的框架层更新。原则是先把基础框架整理稳定，再继续添加角色、菜单、日志、代码生成等通用功能。

## 目标

- 保留 Ktor + Ktorm 的轻量后端风格。
- 先更新框架基础设施，不急着添加业务功能。
- 将当前骨架整理成可持续扩展的后台管理框架。
- 后续能力参考 RuoYi，但不照搬 Spring 体系的实现方式。

## 架构决策：多租户（全局配置）

本框架面向通用后台场景，多租户数据隔离方式通过**部署级全局配置**选择，同一实例只启用一种模式，不在运行时按租户混用多种策略。

### 设计原则

- **Catalog 主库固定**：用户、密码、公司、RBAC、公司模块权限、平台日志始终在主库；登录与鉴权只访问主库。
- **用户不在子库**：登录时只有 `userName` / `password`，无法遍历租户库定位用户，因此认证相关数据必须在 Catalog。
- **业务数据随模式变化**：业务表存放位置与隔离方式由 `tenant.mode` 决定，业务代码通过统一入口访问，不直接选择数据库。
- **部署级选择**：`tenant.mode` 在配置文件中设定；已有生产数据后，不应仅靠改配置切换模式（需单独的数据迁移方案）。

### 租户模式

| 模式 | 配置值 | 业务数据位置 | 隔离方式 | 适用场景 |
|------|--------|-------------|----------|----------|
| 字段隔离 | `column` | Catalog 主库 | 业务表带 `company_id`，查询自动过滤 | 通用 SaaS、租户多、运维简单 |
| 分库隔离 | `database` | 每租户独立库 `db_{companyId}` | 物理分库 | 强隔离、单租户数据量大 |

### 配置示例（规划）

```yaml
tenant:
  enabled: true                      # false 时可作为单租户项目部署
  mode: column                       # column | database
  catalog-database: ydb3             # Catalog 主库名
  tenant-db-prefix: "db_"            # mode=database 时租户库前缀
  tenant-id-column: company_id       # mode=column 时业务表租户字段名
```

支持环境变量覆盖，例如 `TENANT_MODE=database`。

### 数据分层

**Catalog 主库（两种模式相同）**

| 数据 | 表（现有/规划） | 说明 |
|------|----------------|------|
| 租户元数据 | `sys_company` | 公司/租户注册信息 |
| 用户 | `sys_user` | `user_name` 建议全局唯一（当前为手机号） |
| 密码 | `sys_password` | 认证凭证 |
| 权限定义 | `sys_permission` | 菜单、接口权限（可演进 RBAC，不重复建 `sys_menu`） |
| 公司模块开通 | `sys_company_permission` | 租户可使用哪些模块（平台层） |
| RBAC | `sys_role`、`sys_role_menu` 等 | 用户在公司内的角色与权限 |
| 平台日志 | 登录日志、操作日志（待建） | 集中审计 |

**业务数据（随 `tenant.mode` 变化）**

| 模式 | 存放 | 实体约定 |
|------|------|----------|
| `column` | Catalog 主库 | 业务表必须含 `company_id`（或配置的租户字段） |
| `database` | `db_{companyId}` | 一库一租户，业务表可不重复存 `company_id` |

示例：`sys_computer` 等设备/订单类表属于业务数据；`column` 模式下暂在主库时需逐步迁移约定， `database` 模式下应在租户库。

### 请求链路

```
登录
  → Catalog：查 sys_user / sys_password
  → Session/JWT 写入 userId、companyId
  → 返回 token

已登录业务请求
  → 鉴权（Catalog 或 Redis 缓存权限）
  → tenant(ctx) 按 mode 路由业务库
       column   → Catalog + 自动附加 company_id 条件
       database → mysql("{prefix}{companyId}")
  → 执行业务逻辑
```

### 数据访问约定（规划）

业务与框架代码不直接调用 `mysql()`，统一使用：

```kotlin
catalog()              // 平台数据：用户、公司、RBAC
tenant(ctx)            // 业务数据：按 tenant.mode 路由
```

`TenantContext` 从 `AppSession` 获取 `companyId`；模式本身来自全局配置，无需写入 Session。

实体分类约定：

- `@CatalogEntity`：仅存在于 Catalog 主库。
- `@TenantEntity`：业务表，随 `tenant.mode` 存放于主库（带 `company_id`）或租户库。

### 初始化与迁移（规划）

| 模式 | Catalog 迁移 | 租户迁移 |
|------|-------------|----------|
| `column` | 平台表 + 业务表（含 `company_id`） | 新公司仅插入 `sys_company` |
| `database` | 仅平台表 | 新公司：建库 `db_{id}` + 执行租户库 schema/seed |

SQL 目录规划：

```
resources/db/
  migration/catalog/  # 主库 Flyway 迁移（已定）
  migration/tenant/   # mode=database 时租户库 Flyway 模板
```

### 与权限模型的关系

- **两层权限均在 Catalog**：
  - `sys_company_permission`：该公司开通了哪些模块（平台卖给租户）。
  - `sys_role` + `sys_role_menu`：用户在公司内能做什么（租户内 RBAC）。
- 权限校验在路由 DSL 层声明，实现查 Catalog 或 Redis 缓存，与 `tenant.mode` 无关。
- `authenticate("admin")` / `authenticate("basic")` 表示认证分组；细粒度权限走权限编码，二者不替代。

### 实施说明

- **当前阶段**：本文档仅确立架构与配置约定，**暂不实现** `TenantDataAccess` 代码；现有业务仍可使用 `mysql()`，后续阶段再迁移。
- **推荐实现顺序**：先实现 `column` 模式与抽象接口，再实现 `database` 模式；接口设计预留两种实现。
- **不在本期范围**：按租户配置不同 `mode`（hybrid）、已有实例上无迁移地切换模式。

## 架构决策：数据库迁移（Flyway）

数据库 schema 演进与种子数据初始化统一采用 **Flyway**，不自研版本化 SQL 迁移。

### 选型结论

- **迁移工具**：Flyway（Gradle 插件或 `flyway-core` 编程式调用，实施阶段确定）。
- **替代关系**：逐步取代 `init()` 中手工执行 `ydb.sql` 的方式；`ydb.sql` 内容拆为 Flyway 脚本后废弃。
- **与 Ktorm 关系**：Flyway 管表结构变更；Ktorm 管运行时 ORM 访问，二者分工明确。

### 脚本目录（规划）

```
src/main/resources/db/
  migration/catalog/     # Catalog 主库：V1__init_schema.sql、V2__seed_admin.sql …
  migration/tenant/      # mode=database 时租户库模板（新建公司时对 db_{id} 执行）
```

命名遵循 Flyway 约定：`V{version}__{description}.sql`；可重复执行的种子数据慎用，优先幂等 SQL 或单独 seed 脚本 + 版本号管理。

### 执行时机

| 场景 | 方式 |
|------|------|
| 应用启动（Catalog） | `Application.module()` 中在 `init()` 之前/之内调用 Flyway migrate Catalog 库 |
| 新建租户（`database` 模式） | 建库后对 `db_{companyId}` 执行 `migration/tenant` 脚本 |
| 本地 / CI / 生产升级 | 同一套 Flyway 脚本，凭配置连接不同数据源 |

### 与多租户模式的关系

| `tenant.mode` | Catalog（`migration/catalog`） | 租户库（`migration/tenant`） |
|---------------|-------------------------------|------------------------------|
| `column` | 平台表 + 业务表（含 `company_id`） | 不使用 |
| `database` | 仅平台表 | 每个新公司建库后执行 |

### 实施说明

- **当前阶段**：已定 Flyway 选型，**尚未接入**；现有仍使用 `plugins/Init.kt` + `ydb.sql`。
- **迁移步骤**：将 `ydb.sql` 拆为 `V1__catalog_schema.sql` 等；默认管理员/角色/权限写入 seed 迁移或 `V*__seed_*.sql`。
- **注意**：Flyway 脚本一旦发布避免修改已执行版本；结构变更用新 `V{n}__` 脚本。

## 当前已有

- 工程基础：Kotlin 2.4、Ktor 3.5、Gradle、ShadowJar。
- Web 能力：路由、统一异常处理、Jackson 序列化、静态资源、WebSocket。
- 认证基础：JWT 登录认证、`admin` / `basic` 认证分组。
- 依赖注入：Koin。
- 数据访问：Ktorm、MySQL、HikariCP、多数据库连接雏形（`mysql(database)`、`AppSession.db()`，业务尚未按租户模式接入）。
- 缓存基础：Redis/Jedis。
- 通用模型：统一响应、分页响应、基础请求参数、基础实体字段。
- 基础模块：用户、公司/租户、公司权限、设备、密码。
- 初始化能力：启动时 `init()` 检查主库并导入单个 `ydb.sql`（**待改为 Flyway**，见「架构决策：数据库迁移」）。
- 工具扩展：JSON、HTTP 请求封装、文件、日期、Ktorm、拼音、运行环境判断。

## 当前需要先改造

### 1. 配置体系

现状：
- 数据库、Redis、JWT、图片目录等配置主要写在 `Constants.kt`。
- dev/prod/docker 等环境判断已有雏形，但还没有统一配置入口。

改造：
- 增加 `application.yaml` 或 `application.conf`。
- 支持环境变量覆盖敏感配置。
- 将 `Constants.kt` 改成读取配置后的 `AppConfig`。
- 数据库、Redis、JWT secret、端口、上传目录都从配置读取。
- 增加多租户全局配置：`tenant.enabled`、`tenant.mode`、`catalog-database` 等（见「架构决策：多租户」）。

验收：
- 本地、Docker、生产环境能用不同配置启动。
- 代码里不再硬编码密码、secret、主机地址。
- 可通过配置文件选择 `tenant.mode=column` 或 `database`（实现可后续阶段完成）。

### 2. 启动与初始化

现状：
- `init()` 会检查并创建主数据库。
- SQL 初始化还是单个 `ydb.sql`。
- 默认账号逻辑在登录流程里创建，不适合长期维护。

改造：
- 把数据库初始化拆为 Catalog schema/seed 与租户库模板（`database` 模式）。
- 默认账号、默认角色、默认权限从 Flyway seed 迁移脚本创建，不再在登录流程中创建。
- **使用 Flyway** 管理版本化 SQL 迁移（见「架构决策：数据库迁移」）。
- 启动时 Flyway 迁移 Catalog 库；`database` 模式下新建公司时对租户库执行 `migration/tenant`。
- 迁移脚本需幂等、可重复启动；已执行版本不可改写。
- `column` 模式：Catalog 迁移包含业务表；`database` 模式：Catalog 仅平台表。

验收：
- 空库启动能自动执行 Flyway 并创建基础表和默认管理员。
- 已有库启动不会重复插入脏数据。
- 后续升级通过新增 `V{n}__` 脚本完成。
- 两种 `tenant.mode` 各有清晰的 Flyway 迁移路径。

### 3. 安全认证

现状：
- JWT 已有。
- 密码当前仍是明文比对。
- JWT secret 固定，token 生命周期和刷新机制不完整。

改造：
- 密码改为 BCrypt 或 Argon2 哈希。
- JWT secret、issuer、audience、过期时间配置化。
- 增加 refresh token 或 token 续期方案。
- 增加登出 token 失效机制。
- 增加登录失败次数限制和账号锁定。

验收：
- 数据库不存明文密码。
- 登录、刷新、登出流程完整。
- 失效 token 不能继续访问接口。

### 4. 权限模型

现状：
- 有 `sys_permission`、`sys_company_permission`。
- 用户有 `role_id` 字段，但完整角色体系还没有落地。
- 路由只分了 `admin` 和 `basic`，没有细粒度权限。

改造：
- 增加 `sys_role`、`sys_role_menu`；明确单角色（现有 `role_id`）或按需增加 `sys_user_role`。
- 演进现有 `sys_permission` 承载菜单、按钮、接口权限（`type` 字段）；避免与 `sys_company_permission` 重复新建 `sys_menu`，除非迁移时重命名。
- 定义权限编码，例如 `system:user:list`。
- 实现路由权限校验 DSL（Ktor 场景优先 DSL，而非注解式）。
- RBAC 表均在 Catalog 主库，与 `tenant.mode` 无关。

验收：
- 用户登录后可得到菜单树和权限标识。
- 路由可按权限编码拦截。
- 管理员可绕过权限或拥有全部权限。

### 5. 用户与租户基础模块

现状：
- 用户、公司模块已保留。
- 公司权限和设备授权已有基础表。
- 用户、公司服务中还有一些历史逻辑和乱码文案。

改造：
- 统一中文文案编码，修复乱码。
- 用户模块拆分为登录、个人信息、用户管理。
- 公司/租户作为多租户基础单元；租户数据隔离方式遵循全局 `tenant.mode`（见「架构决策：多租户」）。
- 统一新增、修改、删除时的审计字段填充。
- 增加软删除查询默认过滤策略。

验收：
- 用户 CRUD、公司 CRUD 可作为框架基础模块。
- 审计字段自动维护。
- 文案和接口返回稳定统一。

### 6. 数据访问规范

现状：
- Ktorm 扩展能力比较丰富。
- Service 中直接写较多查询逻辑。
- DTO、VO、Query、Entity 边界不清晰。

改造：
- 约定包结构：`api`、`service`、`entity`、`model`、`dao`、`config`、`auth`、`authorization`、`routing`。
- 增加请求 DTO、响应 VO、查询 Query 的命名规范；实体区分 `@CatalogEntity` / `@TenantEntity`。
- 复杂查询封装到 repository/dao 层。
- 统一分页查询工具。
- 业务数据通过 `catalog()` / `tenant(ctx)` 访问，禁止在 Service 中直接 `mysql()` 选库（新代码遵循，旧代码逐步迁移）。

验收：
- 新模块可以按固定模板创建。
- Controller/API 层不直接拼复杂 SQL。
- 新业务代码不感知 `column` / `database` 差异。

### 7. 日志与审计

现状：
- 有请求日志拦截器。
- 没有登录日志、操作日志、异常日志表。

改造：
- 增加登录日志。
- 增加操作日志。
- 增加异常日志。
- 支持记录操作人、IP、URL、参数摘要、耗时、结果。
- 敏感字段脱敏。

验收：
- 登录成功/失败有记录。
- 关键操作可追踪。
- 异常可定位请求上下文。

### 8. API 文档

现状：
- 没有 OpenAPI/Swagger。

改造：
- 接入 Ktor OpenAPI/Swagger 插件，或维护轻量接口文档生成方案。
- 统一响应模型、认证 Header、错误码说明。

验收：
- 本地启动后可访问接口文档。
- 登录和带 token 调接口流程可在文档中验证。

## 后续再添加的 RuoYi 类能力

这些能力建议等上面的框架更新完成后再做：

- 角色管理。
- 菜单管理。
- 字典管理。
- 参数配置。
- 部门/岗位管理。
- 在线用户。
- 缓存监控。
- 定时任务。
- 文件管理。
- 代码生成。
- 数据权限。
- 防重复提交。
- 接口限流。
- Excel 导入导出模板化。

## 推荐实施顺序

### 阶段一：框架可配置

1. 增加配置文件和配置读取（含 `tenant.mode` 等多租户项）。
2. 移除硬编码配置。
3. 接入 **Flyway**：拆分 `ydb.sql` 为 `migration/catalog`，调整 `init()` / 启动流程。
4. 定义 `TenantMode`、`TenantContext`、`TenantDataAccess` 接口骨架（可先不接入业务）。
5. 保证 `gradlew test` 通过。

### 阶段二：安全基础

1. 密码哈希。
2. JWT 配置化。
3. token 过期和刷新。
4. 登出失效。
5. 登录失败限制。

### 阶段三：RBAC 基础

1. 设计角色、权限、角色菜单表（Catalog 主库；演进 `sys_permission`）。
2. 实现权限编码。
3. 实现菜单树（结合 `sys_company_permission` 过滤）。
4. 实现路由权限拦截。
5. 实现 `column` 模式的 `TenantDataAccess`（默认优先）。

### 阶段四：系统管理闭环

1. 用户管理。
2. 角色管理。
3. 菜单/权限管理。
4. 公司/租户管理（含 `database` 模式建库流程）。
5. 实现 `database` 模式的 `TenantDataAccess`。
6. 字典和参数配置。

### 阶段五：运维和开发效率

1. 操作日志和登录日志。
2. OpenAPI 文档。
3. 文件上传。
4. 代码生成。
5. 定时任务。

## 当前不建议立刻做

- 不建议马上做业务模块。
- 不建议马上做代码生成。
- 不建议先做前端管理页。
- 不建议在权限模型未稳定前做数据权限。
- 不建议继续在 `Constants.kt` 堆配置。
- 不建议第一期实现按租户混用 `column` / `database`（hybrid）。
- 不建议在无迁移方案时仅靠改配置切换 `tenant.mode`。

## 第一批具体任务

1. 新增 `AppConfig` 和配置文件（含 `tenant.enabled`、`tenant.mode` 等占位项）。
2. 改造 `DatabaseFactory`、Redis、JWT 使用配置。
3. 引入 **Flyway** 依赖与 Catalog 库首批迁移脚本（由 `ydb.sql` 拆分）。
4. 修复乱码注释和接口提示文案。
5. 把默认管理员创建从登录逻辑迁到 Flyway seed 脚本。
6. 引入密码哈希。
7. 设计 RBAC 表结构草案（Catalog 主库；与 `sys_permission` 演进方案对齐）。
8. 补登录、用户查询、权限校验的基础测试。
9. 确认本文档「架构决策：多租户」与后续实现的接口命名（`catalog()` / `tenant(ctx)`）一致。
