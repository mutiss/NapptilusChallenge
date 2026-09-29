package com.mutissx.napptilusrickandmorty.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Network interceptor that rewrites `Cache-Control` on successful GET responses with a
 * max-age tuned per endpoint, so HTTP caching behaviour is owned by the app instead of
 * depending on whatever headers the server (or its CDN) happens to send.
 *
 * - Character pages: 1 hour (new characters are rare, but a list should not look frozen).
 * - Single character: 1 day.
 * - Episodes: 7 days (aired episodes never change).
 */
class CacheControlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (request.method != "GET" || !response.isSuccessful) return response

        val maxAgeSeconds = maxAgeFor(request.url.encodedPath) ?: return response

        return response.newBuilder()
            .removeHeader("Pragma")
            .header("Cache-Control", "public, max-age=$maxAgeSeconds")
            .build()
    }

    private fun maxAgeFor(path: String): Long? = when {
        path.contains("/episode/") -> TimeUnit.DAYS.toSeconds(7)
        CHARACTER_DETAIL_PATH.containsMatchIn(path) -> TimeUnit.DAYS.toSeconds(1)
        path.contains("/character") -> TimeUnit.HOURS.toSeconds(1)
        else -> null
    }

    private companion object {
        val CHARACTER_DETAIL_PATH = Regex("""/character/\d+$""")
    }
}
