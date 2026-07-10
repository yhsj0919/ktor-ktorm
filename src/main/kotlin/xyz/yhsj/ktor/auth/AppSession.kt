package xyz.yhsj.ktor.auth

import xyz.yhsj.ktor.persistence.entity.user.User
import java.util.*

class AppSession(var user: User? = null, var time: Long = Date().time)




