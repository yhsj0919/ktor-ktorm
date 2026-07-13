package xyz.yhsj.ktor.api.model.request.user

data class UserUpdateRequest(
    val nickName: String? = null,
    val password: String? = null,
    val oldPassword: String? = null,
)
