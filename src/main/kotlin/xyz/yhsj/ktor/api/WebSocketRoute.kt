package xyz.yhsj.ktor.api

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import xyz.yhsj.ktor.entity.websocket.WsClient
import xyz.yhsj.ktor.entity.websocket.WsMsg
import xyz.yhsj.ktor.ext.WsUtils
import xyz.yhsj.ktor.ext.fromJson
import xyz.yhsj.ktor.ext.json


fun Route.webSocketRoute() {

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
                                println("添加客户端：$json")
                                val wsClient = fromJson<WsClient>(wsMsg.data.toString())
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
                        e.printStackTrace()
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
                    println("Binary frame received.")
                }

                is Frame.Close -> {
                    println("Close frame received.")
                }

                is Frame.Ping -> {
                    println("Ping frame received.")
                }

                is Frame.Pong -> {
                    println("Pong frame received.")
                }
            }
        }
    }
}