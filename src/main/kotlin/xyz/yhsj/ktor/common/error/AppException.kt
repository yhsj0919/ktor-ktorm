package xyz.yhsj.ktor.common.error

/**
 * 应用异常
 */
class AppException(var code: Int = 500, message: String?) : Exception(message) {
}
