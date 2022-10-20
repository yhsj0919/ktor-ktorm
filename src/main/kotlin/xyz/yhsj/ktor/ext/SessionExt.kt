package xyz.yhsj.ktor.ext

import io.ktor.server.application.*
import io.ktor.server.sessions.*


/**
 * 获取sessionId
 */
fun ApplicationCall.sessionId(key: String = "App_SESSION"): String? {
    return this.request.cookies[key]
}

/**
 * 获取session
 */
inline fun <reified T : Any> ApplicationCall.session(): T {
    return this.sessions.get() ?: new()
}

/**
 * 获取session
 */
inline fun <reified T : Any> ApplicationCall.setSession(value: T) {
    return this.sessions.set(value)
}

/**
 * 获取session
 */
inline fun <reified T> ApplicationCall.sessionOrNull(): T? {
    return this.sessions.get()
}