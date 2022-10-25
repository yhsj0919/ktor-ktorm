package xyz.yhsj.ktor.ext

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.ktorm.jackson.KtormModule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


var jackson = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KtormModule())
    .registerModule(JavaTimeModule().apply {
        addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
    })


//json序列化
fun Any?.json(): String = if (this != null) {
    jackson.writeValueAsString(this)
} else {
    ""
}

//json反序列化
inline fun <reified T> fromJson(json: String): T {

    return jackson.readValue(json, T::class.java)

}
inline fun <reified T> String?.toModel(): T {

    return jackson.readValue(this, T::class.java)
}
