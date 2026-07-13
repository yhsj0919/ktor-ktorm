package xyz.yhsj.ktor.api.model.request.user

import jakarta.validation.constraints.NotNull

data class UserDeleteRequest(
    @field:NotNull(message = "用户 ID 不能为空")
    val id: Long? = null,
)
