package com.rulebook.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import org.koin.dsl.module
import okio.Path.Companion.toOkioPath

/**
 * Koin module for Coil image loading configuration.
 *
 * Provides a configured ImageLoader with:
 * - Disk cache (50MB) for offline image availability
 * - Memory cache for in-session performance
 * - OkHttp-backed network fetcher for reliable downloads
 */
val coilModule = module {
    single {
        ImageLoader.Builder(get<Context>())
            .diskCache {
                DiskCache.Builder()
                    .directory(get<Context>().cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB disk cache
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(get<Context>(), 0.25) // 25% of available memory
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
