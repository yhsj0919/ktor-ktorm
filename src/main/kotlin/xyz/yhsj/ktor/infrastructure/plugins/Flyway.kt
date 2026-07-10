package xyz.yhsj.ktor.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import xyz.yhsj.ktor.common.util.logger
import xyz.yhsj.ktor.persistence.database.migrateDatabase

private val FlywayPlugin = createApplicationPlugin("Flyway") {
    val result = migrateDatabase()
    logger.info("MySQL 连接成功")
    logger.info(
        "数据库迁移完成：成功执行 {} 个迁移，当前版本 {}",
        result.migrationsExecuted,
        result.currentVersion ?: "无版本迁移"
    )
}

/** 安装 Flyway 数据库迁移插件。迁移失败时终止应用启动。 */
fun Application.configureFlyway() {
    install(FlywayPlugin)
}
