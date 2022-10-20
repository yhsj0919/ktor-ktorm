package xyz.yhsj.ktor.redis


import io.ktor.server.application.*
import io.ktor.util.*
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.ext.logger
import java.net.URI
import java.time.Duration


@KtorDsl
data class RedisConfig(
    var url: String? = null,
    //客户端连接超时
    var connectionTimeout: Int = Protocol.DEFAULT_TIMEOUT,
    //客户端读写超时
    var soTimeout: Int = Protocol.DEFAULT_TIMEOUT,


    //资源池最大连接数
    var maxTotal: Int = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL,
    //最大空闲连接数
    var maxIdle: Int = GenericObjectPoolConfig.DEFAULT_MAX_IDLE,
    //最小空闲连接数
    var minIdle: Int = GenericObjectPoolConfig.DEFAULT_MIN_IDLE,
    //是否开启JMX监控，建议开启
    var jmxEnabled: Boolean = GenericObjectPoolConfig.DEFAULT_JMX_ENABLE,
    //当资源池用今后调用者是否等待，默认true
    var blockWhenExhausted: Boolean = GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED,
    var maxWaitMillis: Long = GenericObjectPoolConfig.DEFAULT_MAX_WAIT.toMillis(),
    //向资源池借用资源时，检查连接有效性
    var testOnBorrow: Boolean = GenericObjectPoolConfig.DEFAULT_TEST_ON_BORROW,
    //向资源池归还资源时，检查连接有效性
    var testOnReturn: Boolean = GenericObjectPoolConfig.DEFAULT_TEST_ON_RETURN,
    //资源创建时，检查连接有效性
    var testOnCreate: Boolean = GenericObjectPoolConfig.DEFAULT_TEST_ON_CREATE,

    )


var RedisPlugin = createApplicationPlugin(name = "Redis", ::RedisConfig) {
    println("Redis is installed!")
    Redis.init(pluginConfig)
}

object Redis {
    private lateinit var pool: JedisPool

    fun init(redisConfig: RedisConfig): Redis {


        println(">>>>>>>Redis Init>>>>>>>>>")

        val config = JedisPoolConfig()
        config.maxTotal = redisConfig.maxTotal
        config.maxIdle = redisConfig.maxIdle
        config.minIdle = redisConfig.minIdle
        config.jmxEnabled = redisConfig.jmxEnabled
        config.blockWhenExhausted = redisConfig.blockWhenExhausted
        config.setMaxWait(Duration.ofMillis(redisConfig.maxWaitMillis))

        config.testOnBorrow = redisConfig.testOnBorrow
        config.testOnReturn = redisConfig.testOnReturn
        config.testOnCreate = redisConfig.testOnCreate
        pool = JedisPool(
            config,
            URI(redisConfig.url),
            redisConfig.connectionTimeout,
            redisConfig.soTimeout
        )
//        pool = JedisPool(JedisPoolConfig(), url)
        return this
    }

    private  fun <T> client(block:  (Jedis) -> T): T? {

        var client: Jedis? = null

        return try {
            client = pool.resource
//            println(">>>>>>>Redis 创建>>>>>${client}>>>>")
            block(client)
        } catch (e: Exception) {
//            e.printStackTrace()
            null
        } finally {
//            println(">>>>>>>Redis 关闭>>>>>$client>>>>")
            client?.close()
        }


    }

    /**
     * 添加
     */
     fun set(key: String?, value: String?, params: SetParams?): String? {

        return client {
            it.set(key, value, params)
        }


    }

    /**
     * 添加
     */
     fun set(key: String?, value: String?): String? {

        return client {
            it.set(key, value)
        }
    }

    /**
     * 查询Key
     */
     fun keys(key: String?): MutableSet<String>? {
        return client {
            it.keys(key)
        }
    }

    /**
     * 获取
     */
     fun get(key: String?): String? {

        return client {
            it.get(key)
        }

    }

    /**
     * 删除
     */
     fun del(key: String?): Long? {
        return client {
            it.del(key)
        }
    }

    /**
     * 关闭
     */
    fun close() {
        return pool.close()
    }
}

