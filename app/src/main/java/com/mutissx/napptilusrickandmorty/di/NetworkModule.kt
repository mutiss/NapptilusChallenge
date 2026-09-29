package com.mutissx.napptilusrickandmorty.di

import com.mutissx.napptilusrickandmorty.BuildConfig
import com.mutissx.napptilusrickandmorty.data.remote.CacheControlInterceptor
import com.mutissx.napptilusrickandmorty.data.remote.OfflineCacheInterceptor
import com.mutissx.napptilusrickandmorty.data.remote.RateLimitRetryInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private const val HTTP_CACHE_DIR = "http_cache"
private const val HTTP_CACHE_SIZE_BYTES = 20L * 1024L * 1024L
private const val TIMEOUT_SECONDS = 15L

const val IMAGE_HTTP_CLIENT = "image_http_client"

val networkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
    }

    single { CacheControlInterceptor() }

    single { OfflineCacheInterceptor() }

    single { RateLimitRetryInterceptor() }

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
            // Application interceptor: waits out a 429 instead of failing the page load.
            .addInterceptor(get<RateLimitRetryInterceptor>())
            // Network interceptor: decides how long each fresh response may be cached.
            .addNetworkInterceptor(get<CacheControlInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // Coil's client. Kept apart from the API client: Coil has its own disk cache, so it must not
    // go through the HTTP cache or the API's cache-control rewriting. Avatars hit the same rate
    // limit as the API, though, so it shares the 429 retry.
    single(named(IMAGE_HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .addInterceptor(get<RateLimitRetryInterceptor>())
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
