package xyz.yhsj.ktor.api.model.response

/** 业务响应码。HTTP 状态码表示协议结果，这里的响应码表示业务状态。 */
object ResponseCode {
    const val SUCCESS = 200
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val CONFLICT = 409
    const val SYSTEM_NOT_INITIALIZED = 10001
}
