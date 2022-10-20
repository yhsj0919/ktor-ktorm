package xyz.yhsj.ktor.entity.websocket

data class WsMsg(
    // 消息类型:0注册客户端，1消息
    var type: Int? = null,
    var data: Any? = null,
    //分发，0，不分发，1，群发，2，单发
    var distribute: Int? = null,
    //来源用户ID
    var fromU: String? = null,
    //目标ID
    var toU: String? = null,
    //公司id
    var companyId: String? = null,
)
