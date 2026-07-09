package xyz.yhsj.ktor.auth

import xyz.yhsj.ktor.entity.user.User
import java.util.*

class AppSession(var user: User? = null, var time: Long = Date().time)




