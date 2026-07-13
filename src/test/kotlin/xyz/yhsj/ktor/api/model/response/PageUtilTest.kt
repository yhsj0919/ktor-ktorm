package xyz.yhsj.ktor.api.model.response

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PageUtilTest {
    @Test
    fun `page metadata is calculated from zero based page`() {
        val page = PageUtil(0, 2, 5, listOf("one", "two"))

        assertTrue(page.isFirst)
        assertEquals(false, page.isLast)
        assertEquals(3, page.totalPages)
        assertEquals(2, page.numberOfElements)
    }

    @Test
    fun `negative page is normalized to first page`() {
        val page = PageUtil(-1, 10, 1, emptyList())

        assertEquals(0, page.page)
        assertTrue(page.isFirst)
    }

    @Test
    fun `invalid page size is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            PageUtil(0, 0, 1, emptyList())
        }
    }
}
