package com.rulebook.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromString returns LIGHT for light string`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString("light"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString("LIGHT"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString("Light"))
    }

    @Test
    fun `fromString returns DARK for dark string`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromString("dark"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromString("DARK"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromString("Dark"))
    }

    @Test
    fun `fromString returns SYSTEM for system string`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("system"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("SYSTEM"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("System"))
    }

    @Test
    fun `fromString returns SYSTEM for null`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString(null))
    }

    @Test
    fun `fromString returns SYSTEM for invalid string`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("invalid"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString(""))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("unknown"))
    }

    @Test
    fun `fromString trims whitespace from input`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString(" LIGHT "))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromString("  light  "))
        assertEquals(ThemeMode.DARK, ThemeMode.fromString(" DARK "))
        assertEquals(ThemeMode.DARK, ThemeMode.fromString("\tdark\t"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString(" SYSTEM "))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("\n system \n"))
    }

    @Test
    fun `fromString handles whitespace-only strings`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("   "))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("\t"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromString("\n"))
    }

    @Test
    fun `enum values are correct`() {
        val values = ThemeMode.entries
        assertEquals(3, values.size)
        assertEquals(ThemeMode.LIGHT, values[0])
        assertEquals(ThemeMode.DARK, values[1])
        assertEquals(ThemeMode.SYSTEM, values[2])
    }
}
