package xyz.yhsj.ktor.ext

import io.ktor.websocket.*
import xyz.yhsj.ktor.entity.websocket.WsClient
import xyz.yhsj.ktor.entity.websocket.WsMsg

object WsUtils {
    private val stoneClients = HashMap<WsClient, DefaultWebSocketSession>()
    private val managerClients = HashMap<WsClient, DefaultWebSocketSession>()
    private val closeClients = ArrayList<WsClient>()

    /**
     * 获取石头客户端
     */
    suspend fun getStoneClient(companyId: String): List<WsClient> {
        //清理一下失效的客户端
        stoneClients.cleanClient()

        return stoneClients.keys.filter { it.companyId == companyId }.toList()
    }

    suspend fun add(ws: DefaultWebSocketSession, client: WsClient) {
        if (client.type == 0) {
            if (ws !in stoneClients.values) {
                stoneClients[client] = ws
                println("已添加客户端")
            } else {
                println("客户端已存在")
            }

            //清理一下失效的客户端
            stoneClients.cleanClient()
        }

        if (client.type == 1) {
            if (ws !in managerClients.values) {
                managerClients[client] = ws
                try {
                    ws.send(mapOf("code" to 200, "msg" to "已连接").json())
                } catch (e: Exception) {
                    println("客户端退出了，删掉")
                }

                println("已添加客户端")
            } else {
                println("客户端已存在")
            }
        }

    }

    suspend fun send(data: WsMsg) {

        if (data.distribute == 1) {
            managerClients.filterKeys { it.companyId == data.companyId }.forEach {
                try {
                    it.value.send(mapOf("code" to 201, "msg" to "已连接", "data" to data.data).json())
                } catch (e: Exception) {
                    println("客户端退出了，删掉")
                    closeClients.add(it.key)
                }
            }

        }

        if (data.distribute == 2) {
            managerClients.filterKeys { it.companyId == data.companyId && it.userId == data.toU }.forEach {
                try {
                    it.value.send(mapOf("code" to 201, "msg" to "已连接", "data" to data.data).json())
                } catch (e: Exception) {
                    println("客户端退出了，删掉")
                    closeClients.add(it.key)
                }
            }
        }

        closeClients.forEach {
            if (it.type == 0) {
                stoneClients.remove(it)
            }
            if (it.type == 1) {
                managerClients.remove(it)
            }
        }

        closeClients.clear()
    }

}

/**
 * 清理一下失效的客户端
 */
suspend fun HashMap<WsClient, DefaultWebSocketSession>.cleanClient() {
    val closeClients = ArrayList<WsClient>()

    //清理一下失效的客户端
    this.forEach {
        try {
            it.value.send(mapOf("code" to 200, "msg" to "已连接").json())
        } catch (e: Exception) {
            println("客户端退出了，删掉")
            closeClients.add(it.key)
        }
    }

    closeClients.forEach {
        this.remove(it)
    }
    closeClients.clear()
}

