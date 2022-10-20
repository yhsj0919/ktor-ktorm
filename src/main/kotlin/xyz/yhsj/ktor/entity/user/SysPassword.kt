package xyz.yhsj.ktor.entity.user

import java.util.UUID


data class SysPassword(

    val id: String? = null,
    var user: String? = null,
    var password: String? = null,
)