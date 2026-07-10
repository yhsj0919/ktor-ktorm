package xyz.yhsj.ktor.infrastructure.cache

import io.ktor.server.application.*
import xyz.yhsj.ktor.infrastructure.config.REDIS_HOST
import xyz.yhsj.ktor.infrastructure.config.REDIS_PASSWORD

/** 安装并配置 Redis 插件。 */
fun Application.configureRedis() {
    install(RedisPlugin) {
        url = "redis://:$REDIS_PASSWORD@$REDIS_HOST/0"
    }
}
