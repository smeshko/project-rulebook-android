package com.rulebook.core.network

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for RulebookApiClient configuration.
 */
class RulebookApiClientTest {

    @Test
    fun `createOkHttpClient configures 30 second connect timeout`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = false)

        assertEquals(30_000, client.connectTimeoutMillis)
    }

    @Test
    fun `createOkHttpClient configures 30 second read timeout`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = false)

        assertEquals(30_000, client.readTimeoutMillis)
    }

    @Test
    fun `createOkHttpClient configures 30 second write timeout`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = false)

        assertEquals(30_000, client.writeTimeoutMillis)
    }

    @Test
    fun `createOkHttpClient configures 60 second call timeout`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = false)

        assertEquals(60_000, client.callTimeoutMillis)
    }

    @Test
    fun `createOkHttpClient adds logging interceptor in debug mode`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = true)

        val hasLoggingInterceptor = client.interceptors.any { it is HttpLoggingInterceptor }
        assertTrue("Should have logging interceptor in debug mode", hasLoggingInterceptor)
    }

    @Test
    fun `createOkHttpClient does not add logging interceptor in release mode`() {
        val client = RulebookApiClient.createOkHttpClient(isDebug = false)

        val hasLoggingInterceptor = client.interceptors.any { it is HttpLoggingInterceptor }
        assertTrue("Should not have logging interceptor in release mode", !hasLoggingInterceptor)
    }

    @Test
    fun `createRetrofit configures base URL`() {
        val okHttpClient = RulebookApiClient.createOkHttpClient(isDebug = false)
        val retrofit = RulebookApiClient.createRetrofit(okHttpClient, "https://api.test.com/")

        assertEquals("https://api.test.com/", retrofit.baseUrl().toString())
    }

    @Test
    fun `createRulebookApi returns non-null API instance`() {
        val okHttpClient = RulebookApiClient.createOkHttpClient(isDebug = false)
        val retrofit = RulebookApiClient.createRetrofit(okHttpClient, "https://api.test.com/")
        val api = RulebookApiClient.createRulebookApi(retrofit)

        assertNotNull(api)
    }

    @Test
    fun `json configuration ignores unknown keys`() {
        val jsonString = """{"game_title":"Test","confidence":0.9,"unknown_field":"value"}"""

        // Should not throw exception
        val response = RulebookApiClient.json.decodeFromString(
            com.rulebook.core.network.model.AnalyzeResponse.serializer(),
            jsonString
        )

        assertEquals("Test", response.gameTitle)
    }
}
