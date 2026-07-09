package xyz.yhsj.ktor.ext

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.ktorm.jackson.KtormModule
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var jackson: ObjectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KtormModule())
    .registerModule(JavaTimeModule().apply {
        addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
    })

val gson: Gson =
    GsonBuilder().setDateFormat(DateFormat.LONG).setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC)
        .create()

fun Any?.json(): String = if (this != null) {
    jackson.writeValueAsString(this)
} else {
    ""
}

inline fun <reified T> fromJson(json: String): T {
    return jackson.readValue(json, T::class.java)
}

inline fun <reified T> String?.toModel(): T {
    return jackson.readValue(this, object : TypeReference<T>() {})
}

inline fun <reified T> Any?.toModel(): T {
    return if (this is String) {
        jackson.readValue(this, object : TypeReference<T>() {})
    } else {
        jackson.readValue(this.json(), object : TypeReference<T>() {})
    }
}

fun Any?.gson(): String = if (this != null) {
    gson.toJson(this)
} else {
    ""
}

inline fun <reified T> fromGson(json: String): T {
    val type: Type = object : TypeToken<T>() {}.type
    return gson.fromJson(json, type)
}

inline fun <reified T> String?.gtoModel(): T {
    val type: Type = object : TypeToken<T>() {}.type
    return gson.fromJson(this, type)
}
