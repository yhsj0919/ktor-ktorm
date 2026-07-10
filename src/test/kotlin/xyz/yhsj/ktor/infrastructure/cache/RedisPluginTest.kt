package xyz.yhsj.ktor.infrastructure.cache

import redis.clients.jedis.params.SetParams
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RedisPluginTest {
    @AfterTest
    fun tearDown() {
        Redis.close()
    }

    @Test
    fun `config rejects invalid pool bounds`() {
        val config = RedisConfig().apply {
            maxTotal = 2
            maxIdle = 3
        }

        assertFailsWith<IllegalArgumentException> { config.validate() }
    }

    @Test
    fun `facade delegates commands to installed client`() {
        val client = FakeRedisClient()
        Redis.install(client)

        assertEquals("OK", Redis.set("key", "value"))
        assertEquals("value", Redis.get("key"))
        assertEquals(mutableSetOf("key"), Redis.keys("k*"))
        assertEquals(1, Redis.del("key"))
    }

    @Test
    fun `closing facade closes installed client`() {
        val client = FakeRedisClient()
        Redis.install(client)

        Redis.close()

        assertTrue(client.closed)
    }
}

private class FakeRedisClient : RedisClient {
    private val values = mutableMapOf<String, String>()
    var closed = false

    override fun ping(): String = "PONG"

    override fun set(key: String, value: String, params: SetParams?): String {
        values[key] = value
        return "OK"
    }

    override fun get(key: String): String? = values[key]

    override fun del(vararg keys: String): Long = keys.count { values.remove(it) != null }.toLong()

    override fun scan(pattern: String, count: Int): Set<String> = values.keys.toSet()

    override fun close() {
        closed = true
    }
}
