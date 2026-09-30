package com.mutissx.napptilusrickandmorty.feature.characters.data.remote

import com.mutissx.napptilusrickandmorty.core.network.interceptor.CacheControlInterceptor
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

/** The characters endpoints get the intended lifetimes through the shared interceptor. */
class CharactersCachePoliciesTest {

    private val interceptor = CacheControlInterceptor(charactersCachePolicies.policies)

    private fun intercept(url: String, code: Int = 200, serverCacheControl: String = "no-cache"): Response {
        val request = Request.Builder().url(url).build()
        val chain: Interceptor.Chain = mockk {
            every { request() } returns request
            every { proceed(request) } returns Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("")
                .header("Cache-Control", serverCacheControl)
                .body("{}".toResponseBody(null))
                .build()
        }
        return interceptor.intercept(chain)
    }

    @Test
    fun `given a character list response, when intercepted, then it is cacheable for one hour`() {
        val response = intercept("https://rickandmortyapi.com/api/character/?page=2")

        assertEquals(TimeUnit.HOURS.toSeconds(1).toInt(), response.cacheControl.maxAgeSeconds)
    }

    @Test
    fun `given a single character response, when intercepted, then it is cacheable for one day`() {
        val response = intercept("https://rickandmortyapi.com/api/character/42")

        assertEquals(TimeUnit.DAYS.toSeconds(1).toInt(), response.cacheControl.maxAgeSeconds)
    }

    @Test
    fun `given an episodes response, when intercepted, then it is cacheable for seven days`() {
        val response = intercept("https://rickandmortyapi.com/api/episode/[1,2,3]")

        assertEquals(TimeUnit.DAYS.toSeconds(7).toInt(), response.cacheControl.maxAgeSeconds)
    }

    @Test
    fun `given an unsuccessful response, when intercepted, then the server cache headers are kept`() {
        val response = intercept("https://rickandmortyapi.com/api/character/42", code = 500)

        assertEquals("no-cache", response.header("Cache-Control"))
    }

    @Test
    fun `given an unknown endpoint, when intercepted, then the server cache headers are kept`() {
        val response = intercept("https://rickandmortyapi.com/api/location/1")

        assertEquals("no-cache", response.header("Cache-Control"))
    }
}
