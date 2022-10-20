package xyz.yhsj.ktor.ext

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.entity.Entity
import java.io.InputStream

/**
 * 创建数据库
 */
fun Database.createDatabase(database: String): Boolean {
    this.useConnection { conn ->
        val databaseSql = "create database $database"
        val size = conn.prepareStatement(databaseSql).use { statement ->
            statement.executeUpdate()
        }
        return size >= 1
    }
}

/**
 * 执行sql
 */
fun Database.sql(sql: String): Boolean {
    this.useConnection { conn ->
        val size = conn.prepareStatement(sql).use { statement ->
            statement.executeUpdate()
        }
        return size >= 1
    }
}

/**
 * 判断数据库下面的表是否存在
 * @param tableName 表名
 * @param database 数据库名称，不传则为当前连接的数据库
 */
fun Database.tableExists(tableName: String, database: String? = null): Boolean {
    this.useConnection { conn ->
        val databaseSql =
            "select count(*) count from information_schema.tables where table_schema='${database ?: name}' and table_name='$tableName'"
        val result = conn.prepareStatement(databaseSql).use { statement ->
            statement.executeQuery().asIterable().firstOrNull()
        }
        if (result != null) {
            return result.getInt("count") >= 1
        } else {
            return false
        }
    }
}


/**
 * 判断数据库下面的表是否存在
 * @param database 数据库名称
 */
fun Database.dataBaseExists(database: String): Boolean {
    this.useConnection { conn ->
        val databaseSql =
            "select count(*) count from information_schema.schemata where schema_name='$database'"
        val result = conn.prepareStatement(databaseSql).use { statement ->
            statement.executeQuery().asIterable().firstOrNull()
        }
        if (result != null) {
            return result.getInt("count") >= 1
        } else {
            return false
        }
    }
}

/**
 * 判断数据库下面的表是否存在
 * @param inputStream 文件输入流
 */
suspend fun Database.initWithSqlFile(inputStream: InputStream?): Boolean {
    if (inputStream == null) {
        return false
    }
    logger.info(msg = "数据库[${name}]初始化")

    return this.useConnection { conn ->
        withContext(Dispatchers.IO) {
            inputStream.reader().buffered().useLines { lines ->
                val results = StringBuilder()
                lines.forEach {
                    results.append(it)
                    if (results.endsWith("*/")) {
                        logger.info(results.toString())
                        results.clear()
                    }
                    if (results.startsWith("--")) {
                        logger.info(results.toString())
                        results.clear()
                    }
                    if (results.endsWith(";")) {
                        logger.info("执行语句>>${results.toString()}")

                        conn.prepareStatement(results.toString()).use { statement ->
                            statement.executeUpdate()
                        }
                        results.clear()
                    }
                    if (results.startsWith("/*")) {
                        results.append("\n")
                    }
                }
                logger.info(msg = "数据库[${name}]初始化完成")

            }
            true
        }
    }
}


inline fun <reified T> Entity<*>.lazyFetch(name: String, loader: () -> T): T {
    return this[name] as? T ?: loader().also { this[name] = it }
}

