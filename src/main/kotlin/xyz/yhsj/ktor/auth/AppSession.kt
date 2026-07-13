package xyz.yhsj.ktor.auth

import java.util.*

/** Redis 中保存的登录会话对象，不直接引用 DAO 实体。 */
data class SessionUser(
    val id: Long? = null,
    val userName: String? = null,
    val roleId: Long? = null,
    val nickName: String? = null,
    val type: Int? = null,
)

class AppSession(
    var user: SessionUser? = null,
    var time: Long = Date().time,
)




