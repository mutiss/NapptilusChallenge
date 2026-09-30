package com.mutissx.napptilusrickandmorty.feature.characters.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.DataException
import com.mutissx.napptilusrickandmorty.feature.characters.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.CharacterDto
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.CharactersResponseDto
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.PageInfoDto
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.feature.characters.fake.aCharacter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CharacterPagingSourceTest {

    private val mockApi: RickAndMortyApi = mockk()
    private val memoryCache = CharacterMemoryCache()

    private fun pagingSource(filter: CharacterFilter = CharacterFilter()) =
        CharacterPagingSource(mockApi, filter, memoryCache)

    private fun dto(id: Int) = CharacterDto(id = id, name = "Character $id")

    private fun response(ids: IntRange, next: String? = null) = CharactersResponseDto(
        info = PageInfoDto(count = 100, pages = 5, next = next),
        results = ids.map(::dto)
    )

    private fun refreshParams(key: Int? = null) =
        LoadParams.Refresh(key = key, loadSize = CharacterPagingSource.PAGE_SIZE, placeholdersEnabled = false)

    private fun notFound() = HttpException(Response.error<Any>(404, "".toResponseBody(null)))

    @Test
    fun `given first page load, when load is called with null key, then page 1 is requested and prevKey is null`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(1, any(), any(), any()) } returns response(1..20, next = "page=2")

        // When
        val result = pagingSource().load(refreshParams()) as LoadResult.Page

        // Then
        assertNull(result.prevKey)
        assertEquals(20, result.data.size)
    }

    @Test
    fun `given the response has a next page, when load is called, then nextKey is the following page`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(1, any(), any(), any()) } returns response(1..20, next = "page=2")

        // When
        val result = pagingSource().load(refreshParams()) as LoadResult.Page

        // Then
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `given the response has no next page, when load is called, then nextKey is null`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(5, any(), any(), any()) } returns response(81..86, next = null)

        // When
        val result = pagingSource().load(refreshParams(key = 5)) as LoadResult.Page

        // Then
        assertNull(result.nextKey)
        assertEquals(4, result.prevKey)
    }

    @Test
    fun `given a filter, when load is called, then name, status and gender are sent as lowercase query params`() = runTest {
        // Given
        val filter = CharacterFilter(name = "rick", status = CharacterStatus.ALIVE, gender = CharacterGender.MALE)
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } returns response(1..1)

        // When
        pagingSource(filter).load(refreshParams())

        // Then
        coVerify { mockApi.getCharacters(page = 1, name = "rick", status = "alive", gender = "male") }
    }

    @Test
    fun `given an empty filter, when load is called, then no filter query params are sent`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } returns response(1..1)

        // When
        pagingSource(CharacterFilter(name = "   ")).load(refreshParams())

        // Then
        coVerify { mockApi.getCharacters(page = 1, name = null, status = null, gender = null) }
    }

    @Test
    fun `given the API answers 404 for a search without matches, when load is called, then an empty last page is returned instead of an error`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } throws notFound()

        // When
        val result = pagingSource(CharacterFilter(name = "zzz")).load(refreshParams())

        // Then
        assertTrue(result is LoadResult.Page)
        val page = result as LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test
    fun `given a page loads, when load succeeds, then its characters are stored in the memory cache`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } returns response(1..2)

        // When
        pagingSource().load(refreshParams())

        // Then
        assertNotNull(memoryCache.get(1))
        assertNotNull(memoryCache.get(2))
    }

    @Test
    fun `given api throws IOException, when load is called, then returns LoadResult Error wrapping a DataException with NO_INTERNET`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } throws IOException("Network failure")

        // When
        val result = pagingSource().load(refreshParams())

        // Then
        assertTrue(result is LoadResult.Error)
        val wrapped = (result as LoadResult.Error).throwable
        assertTrue(wrapped is DataException)
        assertEquals(DataError.Network.NO_INTERNET, (wrapped as DataException).error)
    }

    @Test(expected = CancellationException::class)
    fun `given api throws CancellationException, when load is called, then exception is rethrown instead of wrapped`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } throws CancellationException("Cancelled")

        // When
        pagingSource().load(refreshParams())
    }

    @Test
    fun `given anchor on a middle page, when getRefreshKey is called, then returns that page number`() {
        // Given — page 2 (prevKey = 1, nextKey = 3)
        val page = LoadResult.Page(
            data = (21..40).map { aCharacter(id = it) },
            prevKey = 1,
            nextKey = 3
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 5,
            config = PagingConfig(pageSize = CharacterPagingSource.PAGE_SIZE),
            leadingPlaceholderCount = 0
        )

        // When / Then
        assertEquals(2, pagingSource().getRefreshKey(state))
    }

    @Test
    fun `given no anchor position in paging state, when getRefreshKey is called, then returns null`() {
        // Given
        val state = PagingState<Int, Character>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = CharacterPagingSource.PAGE_SIZE),
            leadingPlaceholderCount = 0
        )

        // When / Then
        assertNull(pagingSource().getRefreshKey(state))
    }
}
