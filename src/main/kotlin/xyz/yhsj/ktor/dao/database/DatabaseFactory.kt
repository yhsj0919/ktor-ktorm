package xyz.yhsj.ktor.dao.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.ktorm.database.Database
import xyz.yhsj.ktor.base.config.BaseDataBase
import xyz.yhsj.ktor.base.config.MySqlHost
import xyz.yhsj.ktor.base.config.MySqlPassWord
import xyz.yhsj.ktor.base.config.MySqlUserName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

private const val DATABASE_NAME_PATTERN = "^[A-Za-z0-9_$-]+$"

private data class DatabaseHolder(
    val database: Database,
    val dataSource: HikariDataSource,
)

private val databases = ConcurrentHashMap<String, DatabaseHolder>()

/**
 * 获取指定数据库连接。
 *
 * 保留原有 mysql() 调用方式；同一个数据库只创建一个连接池，避免每次查询重复建立连接。
 */
fun mysql(database: String = BaseDataBase): Database {
    validateDatabaseName(database)
    return databases.computeIfAbsent(database) { createDatabase(it) }.database
}

/** 在 Ktor 应用停止时关闭所有 Hikari 连接池。 */
fun Application.configureDatabaseLifecycle() {
    monitor.subscribe(ApplicationStopped) {
        closeDatabases()
    }
}

/** 手动关闭所有数据库连接池，主要用于测试和应用停止流程。 */
fun closeDatabases() {
    val holders = databases.values.toList()
    databases.clear()
    holders.forEach { holder ->
        runCatching { holder.dataSource.close() }
    }
}

private fun createDatabase(database: String): DatabaseHolder {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = buildJdbcUrl(database)
        username = MySqlUserName
        password = MySqlPassWord

        // 单实例默认值，后续应根据实例数量、数据库 max_connections 和压测结果调整。
        poolName = "ktor-ktorm-$database"
        maximumPoolSize = 10
        minimumIdle = 2
        connectionTimeout = TimeUnit.SECONDS.toMillis(10)
        validationTimeout = TimeUnit.SECONDS.toMillis(5)
        idleTimeout = TimeUnit.MINUTES.toMillis(10)
        maxLifetime = TimeUnit.MINUTES.toMillis(29)
        keepaliveTime = TimeUnit.MINUTES.toMillis(2)

        // MySQL 驱动支持 JDBC4 校验，不需要额外执行 validation query。
        isAutoCommit = true
    })

    return try {
        DatabaseHolder(Database.connect(dataSource), dataSource)
    } catch (error: Throwable) {
        dataSource.close()
        throw error
    }
}

internal fun buildJdbcUrl(database: String): String =
    "jdbc:mysql://$MySqlHost/$database" +
        "?useUnicode=true" +
        "&characterEncoding=utf8" +
        "&serverTimezone=Asia/Shanghai" +
        "&useSSL=false" +
        "&allowPublicKeyRetrieval=true" +
        "&useLocalSessionState=true" +
        "&rewriteBatchedStatements=true"

private fun validateDatabaseName(database: String) {
    require(database.isNotBlank() && DATABASE_NAME_PATTERN.toRegex().matches(database)) {
        "数据库名称不合法：$database"
    }
}
