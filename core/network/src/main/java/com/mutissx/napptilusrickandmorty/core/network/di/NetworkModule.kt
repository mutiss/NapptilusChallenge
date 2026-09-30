package com.mutissx.napptilusrickandmorty.core.network.di

import com.mutissx.napptilusrickandmorty.core.common.ConnectivityObserver
import com.mutissx.napptilusrickandmorty.core.network.BuildConfig
import com.mutissx.napptilusrickandmorty.core.network.connectivity.AndroidConnectivityObserver
import com.mutissx.napptilusrickandmorty.core.network.interceptor.CacheControlInterceptor
import com.mutissx.napptilusrickandmorty.core.network.interceptor.CachePolicies
import com.mutissx.napptilusrickandmorty.core.network.interceptor.OfflineCacheInterceptor
import com.mutissx.napptilusrickandmorty.core.network.interceptor.RateLimitRetryInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

private const val HTTP_CACHE_DIR = "http_cache"
private const val HTTP_CACHE_SIZE_BYTES = 20L * 1024L * 1024L
private const val TIMEOUT_SECONDS = 15L

/** Qualifier of the OkHttp client meant for image loading (no HTTP cache, no cache rewriting). */
const val IMAGE_HTTP_CLIENT = "image_http_client"

/**
 * Shared networking. Features plug in without this module knowing about them:
 * - each one builds its API from the [Retrofit.Builder] here, adding its own base URL;
 * - each one may bind a [CachePolicies] (with a qualifier) to tune its endpoints' cache lifetimes.
 */
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

    single { CacheControlInterceptor(policies = getAll<CachePolicies>().flatMap { it.policies }) }

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
            // Application interceptor: waits out a 429 instead of failing the request.
            .addInterceptor(get<RateLimitRetryInterceptor>())
            // Network interceptor: decides how long each fresh response may be cached.
            .addNetworkInterceptor(get<CacheControlInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // Image loading client. Kept apart from the API client: image loaders have their own disk
    // cache, so it must not go through the HTTP cache or the cache-control rewriting. Images may
    // hit the same rate limit as the API, though, so it shares the 429 retry.
    single(named(IMAGE_HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .addInterceptor(get<RateLimitRetryInterceptor>())
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // A fresh builder per feature: shared client and JSON converter, base URL left to the feature.
    factory {
        Retrofit.Builder()
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
    }

    single<ConnectivityObserver> { AndroidConnectivityObserver(context = androidContext()) }
}
