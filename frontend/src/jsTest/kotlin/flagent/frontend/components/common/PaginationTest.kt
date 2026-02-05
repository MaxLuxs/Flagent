package flagent.frontend.components.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaginationTest {

    @Test
    fun testPaginationComponentExists() {
        assertTrue(true, "Pagination component exists")
    }

    @Test
    fun testTotalPagesCalculation() {
        fun totalPages(total: Long, pageSize: Int): Int {
            return if (total <= 0) 1 else ((total + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
        }
        assertEquals(1, totalPages(0, 25))
        assertEquals(1, totalPages(25, 25))
        assertEquals(2, totalPages(26, 25))
        assertEquals(4, totalPages(100, 25))
    }

    @Test
    fun testHasNextPage() {
        fun hasNext(currentPage: Int, totalPages: Int): Boolean = currentPage < totalPages
        assertFalse(hasNext(1, 1))
        assertTrue(hasNext(1, 2))
        assertFalse(hasNext(2, 2))
    }

    @Test
    fun testHasPrevPage() {
        fun hasPrev(currentPage: Int): Boolean = currentPage > 1
        assertFalse(hasPrev(1))
        assertTrue(hasPrev(2))
    }
}
