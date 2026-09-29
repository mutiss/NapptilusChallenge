package com.mutissx.napptilusrickandmorty.di

import com.mutissx.napptilusrickandmorty.BuildConfig
import com.mutissx.napptilusrickandmorty.data.remote.CacheControlInterceptor
import com.mutissx.napptilusrickandmorty.data.remote.OfflineCacheInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private const val HTTP_CACHE_DIR = "http_cache"
private const val HTTP_CACHE_SIZE_BYTES = 20L * 1024L * 1024L
private const val TIMEOUT_SECONDS = 15L

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
    }

    single { CacheControlInterceptor() }

    single { OfflineCacheInterceptor() }

    single {
        OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(androidApplication().cacheDir, HTTP_CACHE_DIR),
                    maxSize = HTTP_CACHE_SIZE_BYTES
                )
            )
            // Application interceptor: falls back to the cache when the network is unreachable.
            .addInterceptor(get<OfflineCacheInterceptor>())
            // Network interceptor: decides how long each fresh response may be cached.
            .addNetworkInterceptor(get<CacheControlInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
