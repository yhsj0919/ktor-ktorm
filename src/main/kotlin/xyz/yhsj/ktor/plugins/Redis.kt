package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import xyz.yhsj.ktor.REDIS_HOST
import xyz.yhsj.ktor.REDIS_PASSWORD
import xyz.yhsj.ktor.redis.RedisPlugin

/**
 * Redis
 */
fun Application.configureRedis() {
    install(RedisPlugin) {
        url = "redis://:$REDIS_PASSWORD@$REDIS_HOST/0"
        connectionTimeout = 60 * 1000
        minIdle = 4
//        testOnBorrow = true
    }
}