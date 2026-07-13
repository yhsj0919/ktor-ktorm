package xyz.yhsj.ktor.common.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer as GsonJsonSerializer
import com.google.gson.reflect.TypeToken
import org.ktorm.jackson.KtormModule
import org.ktorm.entity.Entity
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

val jackson: ObjectMapper = ObjectMapper().apply {
    enable(SerializationFeature.INDENT_OUTPUT)
    enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
    setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS)
    registerModule(kotlinModule())
    registerModule(KtormModule())
    registerModule(SimpleModule().addSerializer(Entity::class.java, EntityJsonSerializer()))
    registerModule(JavaTimeModule().apply {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(formatter))
        addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(formatter)
        )
    })
//    serializerProvider.setNullKeySerializer(object : JsonSerializer<Any>() {
//        override fun serialize(value: Any, gen: JsonGenerator, serializers: SerializerProvider) {
//            gen.writeFieldName("")
//        }
//    })
}

/** Ktorm 实体默认只遍历已加载属性，这里补齐空值字段但不恢复实体关联。 */
private class EntityJsonSerializer : JsonSerializer<Entity<*>>() {
    private val internalProperties = setOf("entityClass", "properties", "changedProperties")

    override fun serialize(value: Entity<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        value.entityClass.memberProperties.forEach { property ->
            if (property.name in internalProperties) return@forEach
            val getter = property.javaGetter
            if (getter?.isAnnotationPresent(JsonIgnore::class.java) == true) return@forEach

            val name = getter?.getAnnotation(JsonProperty::class.java)?.value
                ?.takeIf { it.isNotEmpty() }
                ?: property.name
            gen.writeFieldName(name)
            val propertyValue = runCatching { value[property.name] }.getOrNull()
            serializers.defaultSerializeValue(propertyValue, gen)
        }
        gen.writeEndObject()
    }
}

private val gsonDateAdapter = object : GsonJsonSerializer<Date>, JsonDeserializer<Date> {
    override fun serialize(src: Date?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return src?.let { JsonPrimitive(DateFormat.getDateInstance(DateFormat.LONG).format(it)) }
            ?: JsonNull.INSTANCE
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
        return DateFormat.getDateInstance(DateFormat.LONG).parse(json.asString)
    }
}

val gson: Gson = GsonBuilder()
    .registerTypeAdapter(Date::class.java, gsonDateAdapter)
    .setPrettyPrinting()
    .excludeFieldsWithModifiers(Modifier.STATIC)
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
