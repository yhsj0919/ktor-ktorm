package xyz.yhsj.ktor.api.route

import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import xyz.yhsj.ktor.websocket.entity.WsClient
import xyz.yhsj.ktor.websocket.entity.WsMsg
import xyz.yhsj.ktor.websocket.extension.WsUtils
import xyz.yhsj.ktor.common.json.fromJson
import xyz.yhsj.ktor.common.json.json
import xyz.yhsj.ktor.common.util.logger


fun Route.webSocketApi() {

        webSocket("/") { // websocketSession
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        try {
                            val json = frame.readText()
//
                            val wsMsg = fromJson<WsMsg>(json)
                            when (wsMsg.type) {
                                0 -> {
                                    logger.info("添加 WebSocket 客户端：{}", json)
                                    val wsClient = fromJson<WsClient>(wsMsg.data.json())
                                    WsUtils.add(this, wsClient)
                                }

                                -1 -> {
                                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                                }

                                1 -> {
                                    //消息
                                    WsUtils.send(wsMsg)
                                }

                                else -> {
                                    outgoing.send(
                                        Frame.Text(
                                            mapOf(
                                                "code" to 501,
                                                "msg" to "无效消息"
                                            ).json()
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("WebSocket 消息处理失败", e)
                            outgoing.send(
                                Frame.Text(
                                    mapOf(
                                        "code" to 500,
                                        "msg" to "服务器错误"
                                    ).json()
                                )
                            )
                        }

                    }

                    is Frame.Binary -> {
                        logger.debug("收到 WebSocket 二进制帧")
                    }

                    is Frame.Close -> {
                        logger.debug("收到 WebSocket 关闭帧")
                    }

                    is Frame.Ping -> {
                        logger.debug("收到 WebSocket Ping 帧")
                    }

                    is Frame.Pong -> {
                        logger.debug("收到 WebSocket Pong 帧")
                    }
                }
            }
        }
}
