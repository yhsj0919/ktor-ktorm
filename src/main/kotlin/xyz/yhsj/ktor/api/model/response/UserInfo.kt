package xyz.yhsj.ktor.api.model.response

import xyz.yhsj.ktor.dao.entity.user.User

/** 对外返回的安全用户信息，不包含密码、权限等敏感字段。 */
data class UserInfo(
    val id: Long?,
    val userName: String?,
    val nickName: String?,
)

fun User.toUserInfo(): UserInfo = UserInfo(
    id = id,
    userName = userName,
    nickName = nickName,
)
