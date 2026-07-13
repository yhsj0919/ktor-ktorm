package xyz.yhsj.ktor.api.model.request.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserLoginRequest(
    @field:NotBlank(message = "用户名不能为空")
    @field:Pattern(regexp = "^[1][3456789]\\d{9}$", message = "用户名必须是手机号")
    val userName: String? = null,

    @field:NotBlank(message = "密码不能为空")
    val password: String? = null,
)
