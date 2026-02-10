package com.rulebook.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import coil3.ImageLoader
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class CoilModuleTest : KoinTest {

    private val imageLoader: ImageLoader by inject()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        startKoin {
            androidContext(context)
            modules(coilModule)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `imageLoader is provided by Koin`() {
        assertNotNull(imageLoader, "ImageLoader should be provided by Koin")
    }

    @Test
    fun `imageLoader has disk cache configured`() {
        val diskCache = imageLoader.diskCache
        assertNotNull(diskCache, "Disk cache should be configured")
    }

    @Test
    fun `imageLoader has memory cache configured`() {
        val memoryCache = imageLoader.memoryCache
        assertNotNull(memoryCache, "Memory cache should be configured")
    }
}
