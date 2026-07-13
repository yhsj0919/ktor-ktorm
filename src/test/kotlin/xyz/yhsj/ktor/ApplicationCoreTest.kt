package xyz.yhsj.ktor

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.api.model.response.ResponseCode
import xyz.yhsj.ktor.api.extension.success
import xyz.yhsj.ktor.base.plugins.configureRouting
import xyz.yhsj.ktor.base.plugins.configureSerialization

class ApplicationCoreTest {
    @Test
    fun `core plugins and base route start successfully`() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            routing {
                get("/health") {
                    call.respond(CommonResp.success(data = mapOf("status" to "UP")))
                }
            }
        }

        val response = client.get("/health")

        assertEquals(200, response.status.value)
        assertTrue(response.headers[HttpHeaders.ContentType]?.startsWith("application/json") == true)
        assertTrue(response.bodyAsText().contains("\"status\""))
        assertTrue(response.bodyAsText().contains("\"UP\""))
    }

    @Test
    fun `unknown route returns HTTP 404`() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }

        val response = client.get("/missing")

        assertEquals(404, response.status.value)
    }

    @Test
    fun `uninitialized system returns HTTP 503`() = testApplication {
        application {
            configureSerialization()
            routing {
                get("/setup") {
                    call.success { CommonResp.systemNotInitialized() }
                }
            }
        }

        val response = client.get("/setup")

        assertEquals(503, response.status.value)
    }

    @Test
    fun `conflict response returns HTTP 409`() = testApplication {
        application {
            configureSerialization()
            routing {
                get("/setup/conflict") {
                    call.success { CommonResp.error(code = ResponseCode.CONFLICT, msg = "管理员已经初始化") }
                }
            }
        }

        val response = client.get("/setup/conflict")

        assertEquals(409, response.status.value)
        assertTrue(response.bodyAsText().contains("管理员已经初始化"))
    }
}
