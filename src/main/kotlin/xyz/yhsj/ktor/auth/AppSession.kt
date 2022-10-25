package xyz.yhsj.ktor.auth

import io.ktor.server.auth.*
import xyz.yhsj.ktor.entity.user.SysUser
import java.util.*


class AppSession(var user: SysUser? = null, var time: Long = Date().time) : Principal




