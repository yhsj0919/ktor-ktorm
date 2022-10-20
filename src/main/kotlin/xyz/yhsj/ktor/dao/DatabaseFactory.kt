package xyz.yhsj.ktor.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.ktorm.database.Database
import xyz.yhsj.ktor.BaseDataBase
import xyz.yhsj.ktor.MySqlHost
import xyz.yhsj.ktor.MySqlPassWord
import xyz.yhsj.ktor.MySqlUserName


private val dbs = HashMap<String, Database>()

@Synchronized
fun mysql(database: String = BaseDataBase): Database {

    return if (dbs.containsKey(database)) {
        dbs[database]!!
    } else {
        val db = Database.connect(HikariDataSource(HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://$MySqlHost/$database"
            username = MySqlUserName
            password = MySqlPassWord
            maximumPoolSize = 3
        }))
        dbs[database] = db
        db
    }
}