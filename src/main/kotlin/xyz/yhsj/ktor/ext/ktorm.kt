package xyz.yhsj.ktor.ext

import org.ktorm.database.Database
import org.ktorm.entity.Entity

/**
 * 创建数据库
 */
fun Database.createDatabase(database: String): Boolean {
    this.useConnection { conn ->
        val databaseSql = "create database $database"
        val smt = conn.createStatement()
        val size = smt.executeUpdate(databaseSql)
        return size == 1
    }
}


inline fun <reified T> Entity<*>.lazyFetch(name: String, loader: () -> T): T {
    return this[name] as? T ?: loader().also { this[name] = it }
}

