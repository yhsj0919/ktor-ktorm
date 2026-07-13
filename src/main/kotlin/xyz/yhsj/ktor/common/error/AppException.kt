package xyz.yhsj.ktor.common.error

/**
 * 应用异常
 */
open class AppException(var code: Int = 500, message: String?) : Exception(message) {
}

/** 权限校验失败，表示用户已经认证但没有访问当前资源的权限。 */
class ForbiddenException(message: String = "无权限") : AppException(403, message)
