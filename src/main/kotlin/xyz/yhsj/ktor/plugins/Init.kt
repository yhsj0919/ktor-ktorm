package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.BaseDataBase
import xyz.yhsj.ktor.dao.mysql
import xyz.yhsj.ktor.ext.*
import xyz.yhsj.ktor.redis.Redis

fun Application.init() {
    launch {
        val res = Redis.set("test connect", "test connect", SetParams.setParams().ex(60))
        if (res != null) {
            logger.info("Redis connect success")
        }
    }

    launch {
        if (mysql("mysql").dataBaseExists(BaseDataBase)) {
            mysql()
        } else {
            mysql("mysql").createDatabase(BaseDataBase)
            val sql = resources("sql", "ydb.sql")
            if (sql != null) {
                mysql().initWithSqlFile(sql.inputStream()!!)
            }
        }
    }
}
