package xyz.yhsj.ktor.base.cache

import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.createApplicationPlugin
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Connection
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.RedisClient as JedisClient
import redis.clients.jedis.params.ScanParams
import redis.clients.jedis.params.SetParams
import java.net.URI
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference
import xyz.yhsj.ktor.common.util.logger

/** Redis 连接和连接池配置。 */
class RedisConfig {
    /** Redis 连接地址，例如 redis://:password@127.0.0.1:6379/0。 */
    var url: String = "redis://127.0.0.1:6379/0"
    /** 建立 Redis 连接的超时时间。 */
    var connectionTimeout: Duration = Duration.ofSeconds(3)
    /** Redis 命令读写超时时间。 */
    var socketTimeout: Duration = Duration.ofSeconds(3)
    /** 连接池允许创建的最大连接数。 */
    var maxTotal: Int = 16
    /** 连接池允许保留的最大空闲连接数。 */
    var maxIdle: Int = 8
    /** 连接池预热时保留的最小空闲连接数。 */
    var minIdle: Int = 2
    /** 连接池耗尽时，获取连接的最大等待时间。 */
    var maxWait: Duration = Duration.ofSeconds(1)
    /** 借出连接前是否检查连接有效性。 */
    var testOnBorrow: Boolean = true
    /** 应用启动时是否立即执行 PING 检查 Redis。 */
    var validateOnStartup: Boolean = true

    /** 校验连接地址、连接池边界和超时时间，尽早暴露配置错误。 */
    internal fun validate() {
        val uri = runCatching { URI(url) }
            .getOrElse { throw IllegalArgumentException("Redis 地址格式不正确", it) }
        require(uri.scheme == "redis" || uri.scheme == "rediss") {
            "Redis 地址协议必须是 redis 或 rediss"
        }
        require(uri.host != null) { "Redis 地址必须包含主机名" }
        require(maxTotal > 0) { "Redis 最大连接数必须大于 0" }
        require(maxIdle >= 0) { "Redis 最大空闲连接数不能小于 0" }
        require(minIdle in 0..maxIdle) { "Redis 最小空闲连接数必须在有效范围内" }
        require(maxIdle <= maxTotal) { "Redis 最大空闲连接数不能超过最大连接数" }
        require(!connectionTimeout.isNegative && !connectionTimeout.isZero) {
            "Redis 连接超时时间必须大于 0"
        }
        require(!socketTimeout.isNegative && !socketTimeout.isZero) {
            "Redis 读写超时时间必须大于 0"
        }
    }
}

/** Redis 最小操作接口，便于业务代码解耦并支持无 Redis 的单元测试。 */
interface RedisClient : AutoCloseable {
    fun ping(): String
    fun set(key: String, value: String, params: SetParams? = null): String?
    fun get(key: String): String?
    fun del(vararg keys: String): Long
    fun scan(pattern: String, count: Int = 100): Set<String>
}

internal class JedisRedisClient(config: RedisConfig) : RedisClient {
    private val client: JedisClient

    init {
        config.validate()
        val uri = URI(config.url)
        val port = if (uri.port == -1) 6379 else uri.port
        val clientConfig = DefaultJedisClientConfig.builder(uri)
            .connectionTimeoutMillis(config.connectionTimeout.toMillis().toInt())
            .socketTimeoutMillis(config.socketTimeout.toMillis().toInt())
            .build()
        val poolConfig = GenericObjectPoolConfig<Connection>().apply {
            maxTotal = config.maxTotal
            maxIdle = config.maxIdle
            minIdle = config.minIdle
            setMaxWait(config.maxWait)
            testOnBorrow = config.testOnBorrow
        }

        client = JedisClient.builder()
            .hostAndPort(HostAndPort(uri.host, port))
            .clientConfig(clientConfig)
            .poolConfig(poolConfig)
            .build()
    }

    override fun ping(): String = client.ping()

    override fun set(key: String, value: String, params: SetParams?): String? =
        if (params == null) client.set(key, value) else client.set(key, value, params)

    override fun get(key: String): String? = client.get(key)

    override fun del(vararg keys: String): Long = client.del(*keys)

    override fun scan(pattern: String, count: Int): Set<String> {
        require(count > 0) { "Redis 扫描数量必须大于 0" }
        val params = ScanParams().match(pattern).count(count)
        val keys = linkedSetOf<String>()
        var cursor = ScanParams.SCAN_POINTER_START
        do {
            val result = client.scan(cursor, params)
            keys += result.result
            cursor = result.cursor
        } while (cursor != ScanParams.SCAN_POINTER_START)
        return keys
    }

    override fun close() = client.close()
}

val RedisPlugin = createApplicationPlugin("Redis", ::RedisConfig) {
    val client = JedisRedisClient(pluginConfig)
    Redis.install(client)

    try {
        if (pluginConfig.validateOnStartup) {
            check(client.ping() == "PONG") { "Redis 启动连接校验失败" }
            logger.info("Redis 连接成功")
        }
    } catch (error: Throwable) {
        Redis.close(client)
        throw error
    }

    application.monitor.subscribe(ApplicationStopped) {
        Redis.close(client)
    }
}

/**
 * Redis 兼容门面。
 *
 * 保留原有静态调用方式，逐步迁移业务代码时无需一次性修改所有调用方。
 */
object Redis {
    private val clientRef = AtomicReference<RedisClient?>()

    internal fun install(client: RedisClient) {
        val previous = clientRef.getAndSet(client)
        previous?.close()
    }

    fun ping(): Boolean = execute("PING") { it.ping() == "PONG" } ?: false

    fun set(key: String?, value: String?, params: SetParams?): String? {
        if (key == null || value == null) return null
        return execute("SET") { it.set(key, value, params) }
    }

    fun set(key: String?, value: String?): String? = set(key, value, null)

    /** 使用 SCAN 分批扫描，避免 KEYS 在大数据量下阻塞 Redis。 */
    fun keys(pattern: String?): MutableSet<String>? {
        if (pattern == null) return null
        return execute("SCAN") { it.scan(pattern).toMutableSet() }
    }

    fun get(key: String?): String? {
        if (key == null) return null
        return execute("GET") { it.get(key) }
    }

    fun del(key: String?): Long? {
        if (key == null) return null
        return execute("DEL") { it.del(key) }
    }

    fun close() {
        clientRef.getAndSet(null)?.close()
    }

    internal fun close(client: RedisClient) {
        if (clientRef.compareAndSet(client, null)) {
            client.close()
        }
    }

    private fun <T> execute(command: String, block: (RedisClient) -> T): T? {
        val client = clientRef.get()
        if (client == null) {
            logger.error("Redis 客户端尚未初始化，命令：{}", command)
            return null
        }
        return runCatching { block(client) }
            .onFailure { logger.error("Redis 命令执行失败，命令：{}", command, it) }
            .getOrNull()
    }
}
