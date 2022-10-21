package xyz.yhsj.ktor.auth

import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import xyz.yhsj.ktor.entity.user.SysCompany
import xyz.yhsj.ktor.entity.user.SysUser
import java.util.*


class AppSession(var user: SysUser? = null, var time: Long = Date().time) : Principal

fun SessionsConfig.setSession() {
    //这里使用了redis管理session
    //替换掉了原有的directorySessionStorage
    cookie<AppSession>("App_SESSION", RedisSessionStorage(timeOut = 24 * 60 * 60L)) {
        cookie.extensions["SameSite"] = "lax"
        //使用Gson 序列化session
        serializer = GsonSessionSerializer(type)
    }
}



