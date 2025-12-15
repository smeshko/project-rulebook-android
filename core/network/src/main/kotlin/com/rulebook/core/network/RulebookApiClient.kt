package com.rulebook.core.network

import com.rulebook.core.network.api.RulebookApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client factory for creating configured network components.
 *
 * Provides OkHttp and Retrofit instances configured with:
 * - 30-second timeouts (connect, read, write)
 * - HTTP logging (debug builds only)
 * - kotlinx.serialization for JSON parsing
 */
object RulebookApiClient {

    private const val TIMEOUT_SECONDS = 30L

    /**
     * JSON configuration for kotlinx.serialization.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Creates a configured OkHttpClient instance.
     *
     * @param isDebug Whether to enable HTTP logging.
     * @return Configured OkHttpClient.
     */
    fun createOkHttpClient(isDebug: Boolean): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .apply {
                if (isDebug) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    /**
     * Creates a configured Retrofit instance.
     *
     * @param okHttpClient The OkHttpClient to use.
     * @param baseUrl The base URL for API requests.
     * @return Configured Retrofit instance.
     */
    fun createRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Creates a RulebookApi instance.
     *
     * @param retrofit The Retrofit instance to use.
     * @return RulebookApi implementation.
     */
    fun createRulebookApi(retrofit: Retrofit): RulebookApi {
        return retrofit.create(RulebookApi::class.java)
    }
}
