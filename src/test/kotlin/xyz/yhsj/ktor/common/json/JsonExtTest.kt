package xyz.yhsj.ktor.common.json

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import xyz.yhsj.ktor.dao.entity.user.User

class JsonExtTest {
    @Test
    fun `jackson round trips java time values`() {
        val source = JsonFixture("demo", LocalDateTime.of(2026, 7, 13, 10, 30, 0))

        val result = fromJson<JsonFixture>(source.json())

        assertEquals(source, result)
    }

    @Test
    fun `jackson rejects trailing json`() {
        assertFails {
            fromJson<JsonFixture>("{\"name\":\"demo\",\"createdAt\":\"2026-07-13 10:30:00\"} {}")
        }
    }

    @Test
    fun `user password id is not exposed in json`() {
        val user = User {
            id = 1
            userName = "13800138000"
            passwordId = 1
        }

        val json = user.json()

        assertFalse(json.contains("passwordId"))
        assertFalse(json.contains("password_id"))

    }
}

private data class JsonFixture(
    val name: String,
    val createdAt: LocalDateTime,
)
