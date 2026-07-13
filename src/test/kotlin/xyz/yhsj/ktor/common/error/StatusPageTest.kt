package xyz.yhsj.ktor.common.error

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import xyz.yhsj.ktor.base.plugins.configureSerialization
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusPageTest {
    @Test
    fun `bad request returns HTTP 400`() = testApplication {
        application {
            configureSerialization()
            install(StatusPages) {
                statusPage()
            }
            routing {
                get("/bad") {
                    throw AppException(400, "参数错误")
                }
            }
        }

        val response: HttpResponse = client.get("/bad")

        assertEquals(400, response.status.value)
    }
}
