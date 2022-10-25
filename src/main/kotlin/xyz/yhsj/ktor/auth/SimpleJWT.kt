package xyz.yhsj.ktor.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.JWT_KEY
import xyz.yhsj.ktor.ext.json
import xyz.yhsj.ktor.redis.Redis

/**
 * jwt 签名
 */
open class SimpleJWT(secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()


    /**
     * 签名
     * @param key      jwt的key
     * @param value      jwt的value，redis的key
     * @param entity 需要存Redis的内容
     * @param timeOut 超时时间，秒
     */
    fun sign(key: String = JWT_KEY, value: String, entity: Any, timeOut: Long = 60 * 60): String {

        Redis.set("session_$value", entity.json(), SetParams.setParams().ex(timeOut))

        return JWT.create().withClaim(key, value).sign(algorithm)
    }
}