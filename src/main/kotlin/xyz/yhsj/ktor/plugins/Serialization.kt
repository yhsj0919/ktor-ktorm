package xyz.yhsj.ktor.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.ktorm.jackson.KtormModule

import java.lang.reflect.Modifier
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun Application.configureSerialization() {
    //序列化
    install(ContentNegotiation) {
//        gson {
//            setDateFormat(DateFormat.LONG)
//            setPrettyPrinting()
//            //序列化空值
////            serializeNulls()
//            //忽略修饰符，这里仅忽略static类型的
//            excludeFieldsWithModifiers(Modifier.STATIC)
//            //序列化Id
////            registerTypeHierarchyAdapter(Id::class.java, IdSerializer())
////            registerTypeHierarchyAdapter(ObjectId::class.java, ObjectIdSerializer())
//        }
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            // ktorm entity serialize support
            registerModule(KtormModule())
            // java LocalDateTime serialize support
            registerModule(JavaTimeModule().apply {
                addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
            })
        }
    }
}

