package xyz.yhsj.ktor.ext

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Modifier
import java.text.DateFormat


val gson: Gson =
    GsonBuilder().setDateFormat(DateFormat.LONG).setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC)
//        .registerTypeHierarchyAdapter(Id::class.java, IdSerializer())
        .create()


//json序列化
fun Any?.json(): String = if (this != null) {
    gson.toJson(this)
} else {
    ""
}

//json反序列化
inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)
inline fun <reified T> String?.toModel(): T = gson.fromJson(this, T::class.java)
