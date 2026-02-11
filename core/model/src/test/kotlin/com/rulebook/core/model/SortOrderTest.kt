package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SortOrderTest {

    @Test
    fun `RECENT has correct display name`() {
        assertEquals("Recent", SortOrder.RECENT.displayName)
    }

    @Test
    fun `ALPHABETICAL has correct display name`() {
        assertEquals("A-Z", SortOrder.ALPHABETICAL.displayName)
    }

    @Test
    fun `DATE_ADDED has correct display name`() {
        assertEquals("Date Added", SortOrder.DATE_ADDED.displayName)
    }

    @Test
    fun `enum values are correctly defined`() {
        val values = SortOrder.entries
        assertEquals(3, values.size)
        assert(values.contains(SortOrder.RECENT))
        assert(values.contains(SortOrder.ALPHABETICAL))
        assert(values.contains(SortOrder.DATE_ADDED))
    }

    @Test
    fun `RECENT is default value`() {
        // This test verifies RECENT is the first enum value, acting as default
        assertEquals(SortOrder.RECENT, SortOrder.entries.first())
    }
}
