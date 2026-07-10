package xyz.yhsj.ktor.persistence.database

import org.flywaydb.core.Flyway
import xyz.yhsj.ktor.infrastructure.config.BaseDataBase
import xyz.yhsj.ktor.infrastructure.config.MySqlPassWord
import xyz.yhsj.ktor.infrastructure.config.MySqlUserName
import xyz.yhsj.ktor.persistence.extension.createDatabase
import xyz.yhsj.ktor.persistence.extension.dataBaseExists

/**
 * 确保业务数据库存在，并执行 Flyway 数据库迁移。
 *
 * 已存在但没有 Flyway 历史表的数据库会从版本 0 建立基线，然后执行首个迁移脚本。
 */
data class DatabaseMigrationResult(
    val migrationsExecuted: Int,
    val currentVersion: String?,
)

fun migrateDatabase(): DatabaseMigrationResult {
    val systemDatabase = mysql("mysql")
    if (!systemDatabase.dataBaseExists(BaseDataBase)) {
        systemDatabase.createDatabase(BaseDataBase)
    }

    val flyway = Flyway.configure()
        .dataSource(buildJdbcUrl(BaseDataBase), MySqlUserName, MySqlPassWord)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .baselineVersion("0")
        .load()
    val result = flyway.migrate()
    return DatabaseMigrationResult(
        migrationsExecuted = result.migrationsExecuted,
        currentVersion = flyway.info().current()?.version?.version,
    )
}
