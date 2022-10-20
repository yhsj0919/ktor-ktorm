package xyz.yhsj.ktor.auth


import io.ktor.server.sessions.*
import kotlinx.coroutines.coroutineScope
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.redis.Redis
import java.io.FileNotFoundException


/**
 * @param timeOut 生命周期，秒
 */
internal class RedisSessionStorage(private val expandKey: String = "session_", private val timeOut: Long = 60 * 60) :
    SessionStorage {

    override suspend fun read(id: String): String {
        return Redis.get("$expandKey$id") ?: throw NoSuchElementException("Session $id not found")
    }

    override suspend fun write(id: String, value: String) {
        coroutineScope {
            Redis.set("$expandKey$id", value, SetParams.setParams().ex(timeOut))
        }
    }

    override suspend fun invalidate(id: String) {
        try {
            Redis.del("$expandKey$id")
        } catch (notFound: FileNotFoundException) {
            throw NoSuchElementException("No session data found for id $id")
        }
    }


}

