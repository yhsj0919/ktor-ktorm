package xyz.yhsj.ktor.websocket.extension

import xyz.yhsj.ktor.common.json.json
import io.ktor.websocket.*
import xyz.yhsj.ktor.websocket.entity.WsClient
import xyz.yhsj.ktor.websocket.entity.WsMsg
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object WsUtils {
    private val appClients = ConcurrentHashMap<WsClient, DefaultWebSocketSession>()
    private val managerClients = ConcurrentHashMap<WsClient, DefaultWebSocketSession>()
    private val closeClients = ConcurrentLinkedQueue<WsClient>()

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
            try {
                sendConnected(ws)
            } catch (error: Exception) {
                closeClients.add(client)
                throw error
            }
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

        while (true) {
            val client = closeClients.poll() ?: break
            if (client.type == 0) {
                appClients.remove(client)
            }
            if (client.type == 1) {
                managerClients.remove(client)
            }
        }
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

suspend fun MutableMap<WsClient, DefaultWebSocketSession>.cleanClient() {
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
