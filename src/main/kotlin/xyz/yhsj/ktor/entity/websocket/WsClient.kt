package xyz.yhsj.ktor.entity.websocket

data class WsClient(
    // 客户端类型0，无人值守，1，管理端
    var type: Int? = null,
    // 公司id
    var companyId: String? = null,
    //用户id
    var userId: String? = null,
    //用户名称
    var userName: String? = null,
)
