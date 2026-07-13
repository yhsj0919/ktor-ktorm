package xyz.yhsj.ktor.api.extension

import io.ktor.http.Parameters
import kotlin.test.Test
import kotlin.test.assertEquals

class FormExtTest {
    @Test
    fun `form map keeps arrays and nested fields`() {
        val params = Parameters.build {
            append("name", "demo")
            append("tags[]", "one")
            append("tags[]", "two")
            append("user[name]", "admin")
        }

        val result = params.toFormMap()

        assertEquals("demo", result["name"])
        assertEquals(listOf("one", "two"), result["tags"])
        assertEquals(mapOf("name" to "admin"), result["user"])
    }
}
