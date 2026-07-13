package xyz.yhsj.ktor.api.extension

import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MultipartExtTest {
    @Test
    fun `part file release is idempotent`() = runBlocking {
        var releaseCount = 0
        val file = PartFile(
            originalFileName = "test.txt",
            contentType = ContentType.Text.Plain,
            stream = ByteReadChannel(ByteArray(0)),
            release = { releaseCount++ },
        )

        file.close()
        file.close()

        assertEquals(1, releaseCount)
    }

    @Test
    fun `multipart close releases every file after a failure`() = runBlocking {
        val released = mutableListOf<String>()
        val first = PartFile("first.txt", null, ByteReadChannel(ByteArray(0))) {
            released += "first"
            error("release failed")
        }
        val second = PartFile("second.txt", null, ByteReadChannel(ByteArray(0))) {
            released += "second"
        }
        val parts = MyPartData(hashMapOf(), arrayListOf(first, second))

        assertFailsWith<IllegalStateException> { parts.close() }
        assertEquals(listOf("first", "second"), released)
    }
}
