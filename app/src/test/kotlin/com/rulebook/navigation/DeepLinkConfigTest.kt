package com.rulebook.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkConfigTest {

    @Test
    fun `scheme is rulebook`() {
        assertEquals("rulebook", DeepLinkConfig.SCHEME)
    }

    @Test
    fun `HOST_RULES is rules`() {
        assertEquals("rules", DeepLinkConfig.HOST_RULES)
    }

    @Test
    fun `HOST_LIBRARY is library`() {
        assertEquals("library", DeepLinkConfig.HOST_LIBRARY)
    }

    @Test
    fun `HOST_CAMERA is camera`() {
        assertEquals("camera", DeepLinkConfig.HOST_CAMERA)
    }

    @Test
    fun `HOST_SETTINGS is settings`() {
        assertEquals("settings", DeepLinkConfig.HOST_SETTINGS)
    }

    @Test
    fun `rules deep link URI pattern is correctly formed`() {
        val uriPattern = "${DeepLinkConfig.SCHEME}://${DeepLinkConfig.HOST_RULES}/{${RulebookNavArgs.GAME_ID}}"
        assertEquals("rulebook://rules/{gameId}", uriPattern)
    }

    @Test
    fun `rules deep link with specific gameId`() {
        val gameId = "chess-classic"
        val expectedUri = "${DeepLinkConfig.SCHEME}://${DeepLinkConfig.HOST_RULES}/$gameId"
        assertEquals("rulebook://rules/chess-classic", expectedUri)
    }

    @Test
    fun `deep link scheme follows Android conventions`() {
        // Android custom schemes should be lowercase and not conflict with standard schemes
        val scheme = DeepLinkConfig.SCHEME
        assertEquals(scheme, scheme.lowercase())
        assert(!scheme.startsWith("http"))
        assert(!scheme.startsWith("https"))
        assert(!scheme.startsWith("file"))
    }
}
