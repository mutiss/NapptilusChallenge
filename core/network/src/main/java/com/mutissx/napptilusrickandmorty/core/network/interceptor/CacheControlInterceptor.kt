package com.mutissx.napptilusrickandmorty.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.time.Duration

/** How long a successful GET whose path matches [pathPattern] may be served from the cache. */
data class CachePolicy(
    val pathPattern: Regex,
    val maxAge: Duration
)

/** A feature's cache policies, contributed to the shared HTTP client through DI. */
data class CachePolicies(val policies: List<CachePolicy>)

/**
 * Network interceptor that rewrites `Cache-Control` on successful GET responses with a max-age
 * chosen by [policies], so HTTP caching behaviour is owned by the app instead of depending on
 * whatever headers the server (or its CDN) happens to send.
 *
 * Policies are checked in order and the first whose pattern matches the path wins, so more
 * specific patterns go first. Unmatched paths keep the server's headers.
 */
class CacheControlInterceptor(
    private val policies: List<CachePolicy>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (request.method != "GET" || !response.isSuccessful) return response

        val path = request.url.encodedPath
        val policy = policies.firstOrNull { it.pathPattern.containsMatchIn(path) } ?: return response

        return response.newBuilder()
            .removeHeader("Pragma")
            .header("Cache-Control", "public, max-age=${policy.maxAge.inWholeSeconds}")
            .build()
    }
}
