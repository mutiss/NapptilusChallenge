package com.mutissx.napptilusrickandmorty

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.mutissx.napptilusrickandmorty.core.network.di.IMAGE_HTTP_CLIENT
import com.mutissx.napptilusrickandmorty.core.network.di.networkModule
import com.mutissx.napptilusrickandmorty.feature.characters.di.charactersModule
import okhttp3.OkHttpClient
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.qualifier.named

private const val IMAGE_CACHE_DIR = "rick_and_morty_image_cache"
private const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
private const val IMAGE_DISK_CACHE_PERCENT = 0.02

class RickAndMortyApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@RickAndMortyApp)
            modules(networkModule, charactersModule)
        }
    }

    /**
     * Single app-wide Coil loader: character avatars are cached in memory (instant re-display
     * while scrolling and for the list → detail shared-element transition) and on disk (they
     * survive process death and work offline).
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            // Retries avatars rejected by the API's rate limit (429) while scrolling fast.
            .okHttpClient { get<OkHttpClient>(named(IMAGE_HTTP_CLIENT)) }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve(IMAGE_CACHE_DIR))
                    .maxSizePercent(IMAGE_DISK_CACHE_PERCENT)
                    .build()
            }
            .crossfade(true)
            .build()
}
