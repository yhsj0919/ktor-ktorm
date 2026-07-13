package xyz.yhsj.ktor.auth.extension

import io.ktor.client.request.get
import io.ktor.server.application.call
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import xyz.yhsj.ktor.base.plugins.configureRouting
import xyz.yhsj.ktor.base.plugins.configurePermission
import xyz.yhsj.ktor.base.plugins.configureSerialization

class PermissionExtTest {
    @Test
    fun `permission denial returns HTTP 403`() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            configurePermission()
            routing {
                get("/secured") {
                    call.requirePermissions(arrayOf("system:user:list"))
                }
            }
        }

        val response = client.get("/secured")

        assertEquals(403, response.status.value)
    }
}
