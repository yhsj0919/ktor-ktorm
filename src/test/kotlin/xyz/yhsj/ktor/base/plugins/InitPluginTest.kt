package xyz.yhsj.ktor.base.plugins

import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals

class InitPluginTest {
    @Test
    fun `init tasks run in registration order`() = testApplication {
        val completed = CompletableDeferred<List<String>>()
        val order = mutableListOf<String>()

        application {
            configureInit {
                task("first") {
                    order += "first"
                }
                task("second") {
                    order += "second"
                    completed.complete(order.toList())
                }
            }
        }

        startApplication()

        assertEquals(listOf("first", "second"), completed.await())
    }
}
