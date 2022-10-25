package xyz.yhsj.ktor.ext

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import xyz.yhsj.ktor.JWT_KEY
import xyz.yhsj.ktor.redis.Redis

inline fun <reified T> JWTCredential.session(claim: String = JWT_KEY): T? {
    val id = payload.getClaim(claim).asString()
    return if (id.isNullOrEmpty()) {
        null
    } else {
        val text = Redis.get("session_$id")
        return text?.toModel()
    }
}

inline fun <reified T> ApplicationCall.session(claim: String = JWT_KEY): T {

    val principal = principal<JWTPrincipal>()
    val id = principal?.payload?.getClaim(claim)?.asString()

    val text = Redis.get("session_$id")
    return text?.toModel() ?: new()

}

/**
 * 获取session
 */
inline fun <reified T> ApplicationCall.sessionOrNull(claim: String = JWT_KEY): T? {
    val principal = principal<JWTPrincipal>()
    val id = principal?.payload?.getClaim(claim)?.asString()
    return if (id.isNullOrEmpty()) {
        null
    } else {
        val text = Redis.get("session_$id")
        text?.toModel()
    }
}