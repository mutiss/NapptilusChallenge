package com.mutissx.napptilusrickandmorty.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Application interceptor that waits out a server's rate limit instead of failing.
 *
 * Rate-limited APIs typically allow a burst of requests and then answer
 * `429 Too Many Requests` with a `Retry-After` of a few seconds. Scrolling a long image list
 * quickly easily exceeds such a burst, and JSON endpoints on the same host usually share it.
 * Without this, the affected images stay on their placeholder and page loads fail.
 *
 * On a 429 it sleeps for `Retry-After` (capped at [maxWaitSeconds]) and retries, at most
 * [maxRetries] times; after that the last 429 is returned to the caller. OkHttp runs
 * interceptors on its own worker threads, so blocking here doesn't touch the main thread.
 */
class RateLimitRetryInterceptor(
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val maxWaitSeconds: Long = DEFAULT_MAX_WAIT_SECONDS,
    private val sleep: (millis: Long) -> Unit = Thread::sleep
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var attempt = 0

        while (response.code == HTTP_TOO_MANY_REQUESTS && attempt < maxRetries) {
            val waitSeconds = retryAfterSeconds(response)
            response.close()
            sleep(TimeUnit.SECONDS.toMillis(waitSeconds))
            // The request may have been dropped while waiting (e.g. its grid cell scrolled away).
            if (chain.call().isCanceled()) throw IOException("Canceled while waiting for rate limit")
            attempt++
            response = chain.proceed(request)
        }
        return response
    }

    private fun retryAfterSeconds(response: Response): Long {
        // Retry-After may also be an HTTP date; this API sends seconds, so anything else falls
        // back to a short default.
        val seconds = response.header(RETRY_AFTER)?.trim()?.toLongOrNull() ?: DEFAULT_WAIT_SECONDS
        return seconds.coerceIn(1, maxWaitSeconds)
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val RETRY_AFTER = "Retry-After"
        const val DEFAULT_MAX_RETRIES = 2
        const val DEFAULT_MAX_WAIT_SECONDS = 10L
        const val DEFAULT_WAIT_SECONDS = 2L
    }
}
