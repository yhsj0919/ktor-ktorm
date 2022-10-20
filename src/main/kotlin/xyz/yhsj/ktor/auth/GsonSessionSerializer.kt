package xyz.yhsj.ktor.auth

import io.ktor.server.sessions.*
import xyz.yhsj.ktor.ext.gson
import xyz.yhsj.ktor.ext.json
import kotlin.reflect.KClass

class GsonSessionSerializer<T : Any>(private val type: KClass<T>) : SessionSerializer<T> {
    override fun deserialize(text: String): T {
        return gson.fromJson(text, type.javaObjectType)
    }

    override fun serialize(session: T): String {
        return session.json().replace("\n", "").replace(" ", "")

    }
}