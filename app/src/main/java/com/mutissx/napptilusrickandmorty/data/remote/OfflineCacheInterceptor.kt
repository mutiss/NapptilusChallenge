package com.mutissx.napptilusrickandmorty.data.remote

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Application interceptor that turns the HTTP cache into an offline fallback.
 *
 * When a GET fails at the transport level (no connectivity, timeout...), it retries the same
 * request forcing the cache, accepting entries up to [maxStaleDays] old. If nothing usable is
 * cached, the original exception is rethrown so the normal error handling still applies.
 */
class OfflineCacheInterceptor(
    private val maxStaleDays: Int = DEFAULT_MAX_STALE_DAYS
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: IOException) {
            if (request.method != "GET") throw e

            val cacheOnlyRequest = request.newBuilder()
                .cacheControl(
                    CacheControl.Builder()
                        .onlyIfCached()
                        .maxStale(maxStaleDays, TimeUnit.DAYS)
                        .build()
                )
                .build()
            val cachedResponse = chain.proceed(cacheOnlyRequest)
            if (cachedResponse.isSuccessful) {
                cachedResponse
            } else {
                // OkHttp answers an unsatisfiable only-if-cached request with a synthetic 504.
                cachedResponse.close()
                throw e
            }
        }
    }

    private companion object {
        const val DEFAULT_MAX_STALE_DAYS = 7
    }
}
