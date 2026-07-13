package xyz.yhsj.ktor.auth

import com.auth0.jwt.JWT
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.base.cache.Redis
import xyz.yhsj.ktor.base.cache.RedisClient
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimpleJWTTest {
    @AfterTest
    fun tearDown() {
        Redis.close()
    }

    @Test
    fun `signed token contains standard claims`() {
        Redis.install(FakeRedisClient())
        val jwt = SimpleJWT("test-secret", "test-issuer", "test-audience")

        val token = jwt.sign(value = "100", entity = mapOf("id" to 100), timeOut = 60)
        val decoded = JWT.decode(token)

        assertEquals("100", decoded.subject)
        assertEquals("100", decoded.getClaim("id").asString())
        assertEquals("test-issuer", decoded.issuer)
        assertEquals(listOf("test-audience"), decoded.audience)
        assertEquals(36, decoded.id.length)
        checkNotNull(decoded.issuedAt)
        checkNotNull(decoded.expiresAt)
        jwt.verifier.verify(token)
    }

    @Test
    fun `verifier rejects token with another audience`() {
        val jwt = SimpleJWT("test-secret", "test-issuer", "test-audience")
        val token = JWT.create()
            .withSubject("100")
            .withIssuer("test-issuer")
            .withAudience("another-audience")
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-secret"))

        assertFailsWith<Exception> { jwt.verifier.verify(token) }
    }
}

private class FakeRedisClient : RedisClient {
    private val values = mutableMapOf<String, String>()

    override fun ping(): String = "PONG"

    override fun set(key: String, value: String, params: SetParams?): String {
        values[key] = value
        return "OK"
    }

    override fun get(key: String): String? = values[key]

    override fun del(vararg keys: String): Long = keys.count { values.remove(it) != null }.toLong()

    override fun scan(pattern: String, count: Int): Set<String> = values.keys.toSet()

    override fun close() = Unit
}
