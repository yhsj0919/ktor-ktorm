package xyz.yhsj.ktor.ext

import io.ktor.websocket.*
import xyz.yhsj.ktor.entity.websocket.WsClient
import xyz.yhsj.ktor.entity.websocket.WsMsg

object WsUtils {
    private val appClients = HashMap<WsClient, DefaultWebSocketSession>()
    private val managerClients = HashMap<WsClient, DefaultWebSocketSession>()
    private val closeClients = ArrayList<WsClient>()

    suspend fun getAppClients(companyId: String): List<WsClient> {
        appClients.cleanClient()
        return appClients.keys.filter { it.companyId == companyId }.toList()
    }

    suspend fun add(ws: DefaultWebSocketSession, client: WsClient) {
        if (client.type == 0) {
            if (ws !in appClients.values) {
                appClients[client] = ws
            }
            appClients.cleanClient()
        }

        if (client.type == 1 && ws !in managerClients.values) {
            managerClients[client] = ws
            sendConnected(ws)
        }
    }

    suspend fun send(data: WsMsg) {
        if (data.distribute == 1) {
            managerClients.filterKeys { it.companyId == data.companyId }.forEach {
                sendToClient(it.key, it.value, data)
            }
        }

        if (data.distribute == 2) {
            managerClients.filterKeys { it.companyId == data.companyId && it.userId == data.toU }.forEach {
                sendToClient(it.key, it.value, data)
            }
        }

        closeClients.forEach {
            if (it.type == 0) {
                appClients.remove(it)
            }
            if (it.type == 1) {
                managerClients.remove(it)
            }
        }

        closeClients.clear()
    }

    private suspend fun sendToClient(client: WsClient, session: DefaultWebSocketSession, data: WsMsg) {
        try {
            session.send(mapOf("code" to 201, "msg" to "connected", "data" to data.data).json())
        } catch (e: Exception) {
            closeClients.add(client)
        }
    }
}

private suspend fun sendConnected(session: DefaultWebSocketSession) {
    session.send(mapOf("code" to 200, "msg" to "connected").json())
}

suspend fun HashMap<WsClient, DefaultWebSocketSession>.cleanClient() {
    val closeClients = ArrayList<WsClient>()

    this.forEach {
        try {
            sendConnected(it.value)
        } catch (e: Exception) {
            closeClients.add(it.key)
        }
    }

    closeClients.forEach {
        this.remove(it)
    }
}
