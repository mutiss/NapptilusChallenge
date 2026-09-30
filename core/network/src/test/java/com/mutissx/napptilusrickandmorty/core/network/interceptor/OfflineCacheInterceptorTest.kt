package com.mutissx.napptilusrickandmorty.core.network.interceptor

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class OfflineCacheInterceptorTest {

    private val interceptor = OfflineCacheInterceptor()
    private val chain: Interceptor.Chain = mockk()

    private val getRequest = Request.Builder().url("https://rickandmortyapi.com/api/character/1").build()

    private fun response(request: Request, code: Int) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("")
        .body("{}".toResponseBody(null))
        .build()

    @Test
    fun `given the network answers, when intercepting, then the network response is returned untouched`() {
        // Given
        val networkResponse = response(getRequest, 200)
        every { chain.request() } returns getRequest
        every { chain.proceed(getRequest) } returns networkResponse

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertSame(networkResponse, result)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun `given the network fails and the cache has the response, when intercepting, then a cache-only request is retried and served`() {
        // Given
        val retried = slot<Request>()
        every { chain.request() } returns getRequest
        every { chain.proceed(capture(retried)) } answers {
            if (retried.captured === getRequest) throw IOException("offline")
            response(retried.captured, 200)
        }

        // When
        val result = interceptor.intercept(chain)

        // Then
        assertEquals(200, result.code)
        assertTrue(retried.captured.cacheControl.onlyIfCached)
    }

    @Test(expected = IOException::class)
    fun `given the network fails and nothing is cached, when intercepting, then the original IOException is rethrown`() {
        // Given — OkHttp answers an unsatisfiable only-if-cached request with a synthetic 504
        val retried = slot<Request>()
        every { chain.request() } returns getRequest
        every { chain.proceed(capture(retried)) } answers {
            if (retried.captured === getRequest) throw IOException("offline")
            response(retried.captured, 504)
        }

        // When
        interceptor.intercept(chain)
    }

    @Test(expected = IOException::class)
    fun `given a non-GET request fails, when intercepting, then it is not retried from cache`() {
        // Given
        val postRequest = Request.Builder()
            .url("https://rickandmortyapi.com/api/character")
            .post("{}".toRequestBody(null))
            .build()
        every { chain.request() } returns postRequest
        every { chain.proceed(postRequest) } throws IOException("offline")

        // When
        interceptor.intercept(chain)
    }
}
