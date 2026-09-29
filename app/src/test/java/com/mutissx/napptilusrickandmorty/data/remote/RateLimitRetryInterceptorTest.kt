package com.mutissx.napptilusrickandmorty.data.remote

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RateLimitRetryInterceptorTest {

    private val sleeps = mutableListOf<Long>()
    private val interceptor = RateLimitRetryInterceptor(maxRetries = 2, maxWaitSeconds = 10, sleep = { sleeps += it })

    private val call: Call = mockk { every { isCanceled() } returns false }
    private val chain: Interceptor.Chain = mockk { every { call() } returns this@RateLimitRetryInterceptorTest.call }

    private val request = Request.Builder().url("https://rickandmortyapi.com/api/character/avatar/1.jpeg").build()

    private fun response(code: Int, retryAfter: String? = null) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("")
        .apply { if (retryAfter != null) header("Retry-After", retryAfter) }
        .body("".toResponseBody(null))
        .build()

    @Test
    fun `given a successful response, when intercepting, then it is returned without retrying or waiting`() {
        // Given
        val ok = response(200)
        every { chain.request() } returns request
        every { chain.proceed(request) } returns ok

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertSame(ok, result)
        verify(exactly = 1) { chain.proceed(any()) }
        assertTrue(sleeps.isEmpty())
    }

    @Test
    fun `given a 429 then a success, when intercepting, then it waits Retry-After and returns the retried response`() {
        // Given
        val ok = response(200)
        every { chain.request() } returns request
        every { chain.proceed(request) } returnsMany listOf(response(429, retryAfter = "3"), ok)

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertSame(ok, result)
        assertEquals(listOf(3_000L), sleeps)
    }

    @Test
    fun `given the API keeps answering 429, when intercepting, then it gives up after maxRetries and returns the 429`() {
        // Given
        every { chain.request() } returns request
        every { chain.proceed(request) } answers { response(429, retryAfter = "1") }

        // When
        val result = interceptor.intercept(chain)

        // Then — 1 original + 2 retries
        assertEquals(429, result.code)
        verify(exactly = 3) { chain.proceed(any()) }
        assertEquals(2, sleeps.size)
    }

    @Test
    fun `given a huge or missing Retry-After, when intercepting, then the wait is capped or defaulted`() {
        // Given
        every { chain.request() } returns request
        every { chain.proceed(request) } returnsMany listOf(
            response(429, retryAfter = "3600"),
            response(429),
            response(200)
        )

        // When
        interceptor.intercept(chain)

        // Then — capped at 10 s, then the 2 s default
        assertEquals(listOf(10_000L, 2_000L), sleeps)
    }

    @Test(expected = IOException::class)
    fun `given the call is canceled while waiting, when intercepting, then it stops instead of retrying`() {
        // Given
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response(429, retryAfter = "1")
        every { call.isCanceled() } returns true

        // When
        interceptor.intercept(chain)
    }
}
