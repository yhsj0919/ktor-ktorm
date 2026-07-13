package xyz.yhsj.ktor.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.base.config.JWT_KEY
import xyz.yhsj.ktor.base.config.SESSION_TIMEOUT
import xyz.yhsj.ktor.common.json.json
import xyz.yhsj.ktor.common.error.AppException
import xyz.yhsj.ktor.base.cache.Redis
import java.util.Date
import java.util.UUID

/**
 * jwt 签名
 */
open class SimpleJWT(
    secret: String,
    private val issuer: String = "ktor-ktorm",
    private val audience: String = "ktor-ktorm-client",
) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()


    /**
     * 签名
     * @param key      jwt的key
     * @param value      jwt的value，redis的key
     * @param entity 需要存Redis的内容
     * @param timeOut 超时时间，秒
     */
    fun sign(key: String = JWT_KEY, value: String, entity: Any, timeOut: Long = SESSION_TIMEOUT): String {
        require(value.isNotBlank()) { "JWT subject 不能为空" }
        require(timeOut > 0) { "JWT 有效期必须大于 0 秒" }

        val result = Redis.set("session_$value", entity.json(), SetParams.setParams().ex(timeOut))
        if (result != "OK") {
            throw AppException(503, "Redis 会话写入失败")
        }

        val issuedAt = Date()
        val expiresAt = Date(issuedAt.time + timeOut * 1000)
        return JWT.create()
            .withSubject(value)
            .withClaim(key, value)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .withIssuer(issuer)
            .withAudience(audience)
            .withJWTId(UUID.randomUUID().toString())
            .sign(algorithm)
    }
}
