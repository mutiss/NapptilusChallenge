package com.mutissx.napptilusrickandmorty.core.network.interceptor

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CacheControlInterceptorTest {

    private val interceptor = CacheControlInterceptor(
        listOf(
            CachePolicy(pathPattern = Regex("""/items/\d+$"""), maxAge = 1.hours),
            CachePolicy(pathPattern = Regex("/items"), maxAge = 5.minutes)
        )
    )

    private fun intercept(request: Request, code: Int = 200): Response {
        val chain: Interceptor.Chain = mockk {
            every { request() } returns request
            every { proceed(request) } returns Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("")
                .header("Cache-Control", "no-cache")
                .body("{}".toResponseBody(null))
                .build()
        }
        return interceptor.intercept(chain)
    }

    private fun get(url: String) = Request.Builder().url(url).build()

    @Test
    fun `given a path matching several policies, when intercepted, then the first matching policy wins`() {
        val response = intercept(get("https://example.com/api/items/42"))

        assertEquals(3_600, response.cacheControl.maxAgeSeconds)
    }

    @Test
    fun `given a path matching only a later policy, when intercepted, then that policy applies`() {
        val response = intercept(get("https://example.com/api/items?page=2"))

        assertEquals(300, response.cacheControl.maxAgeSeconds)
    }

    @Test
    fun `given a path no policy matches, when intercepted, then the server cache headers are kept`() {
        val response = intercept(get("https://example.com/api/other/1"))

        assertEquals("no-cache", response.header("Cache-Control"))
    }

    @Test
    fun `given an unsuccessful response, when intercepted, then the server cache headers are kept`() {
        val response = intercept(get("https://example.com/api/items/42"), code = 500)

        assertEquals("no-cache", response.header("Cache-Control"))
    }

    @Test
    fun `given a non-GET request, when intercepted, then the server cache headers are kept`() {
        val post = Request.Builder().url("https://example.com/api/items/42").post("{}".toRequestBody(null)).build()

        val response = intercept(post)

        assertEquals("no-cache", response.header("Cache-Control"))
    }
}
