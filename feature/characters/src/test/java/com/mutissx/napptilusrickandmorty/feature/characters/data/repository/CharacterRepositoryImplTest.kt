package com.mutissx.napptilusrickandmorty.feature.characters.data.repository

import app.cash.turbine.test
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.CharacterDto
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.CharactersResponseDto
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.EpisodeDto
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.fake.aCharacter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class CharacterRepositoryImplTest {

    private val mockApi: RickAndMortyApi = mockk()
    private lateinit var memoryCache: CharacterMemoryCache
    private lateinit var repository: CharacterRepositoryImpl

    @Before
    fun setUp() {
        memoryCache = CharacterMemoryCache()
        repository = CharacterRepositoryImpl(mockApi, memoryCache)
    }

    // ---- getCharacters ----

    @Test
    fun `given a filter, when getCharacters is collected, then it emits PagingData`() = runTest {
        // Given
        coEvery { mockApi.getCharacters(any(), any(), any(), any()) } returns
            CharactersResponseDto(results = listOf(CharacterDto(id = 1, name = "Rick Sanchez")))

        // When / Then
        repository.getCharacters(CharacterFilter(name = "rick")).test {
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given the same filter twice, when getCharacters is called, then each call returns an independent flow`() {
        val filter = CharacterFilter(name = "morty")

        assertNotSame(repository.getCharacters(filter), repository.getCharacters(filter))
    }

    // ---- getCharacter ----

    @Test
    fun `given the character is not cached, when getCharacter is called, then it is fetched from the API and mapped`() = runTest {
        // Given
        coEvery { mockApi.getCharacter(2) } returns CharacterDto(id = 2, name = "Morty Smith", status = "Alive")

        // When
        val result = repository.getCharacter(2)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Morty Smith", (result as Result.Success).data.name)
    }

    @Test
    fun `given the character is already in the memory cache, when getCharacter is called, then the API is not hit`() = runTest {
        // Given
        val cached = aCharacter(id = 1)
        memoryCache.put(cached)

        // When
        val result = repository.getCharacter(1)

        // Then
        assertEquals(Result.Success(cached), result)
        coVerify(exactly = 0) { mockApi.getCharacter(any()) }
    }

    @Test
    fun `given a fetched character, when getCharacter is called again, then the second call is served from memory`() = runTest {
        // Given
        coEvery { mockApi.getCharacter(3) } returns CharacterDto(id = 3, name = "Summer Smith")

        // When
        repository.getCharacter(3)
        repository.getCharacter(3)

        // Then
        coVerify(exactly = 1) { mockApi.getCharacter(3) }
    }

    @Test
    fun `given api throws a 404 HttpException, when getCharacter is called, then Result Error wraps NOT_FOUND`() = runTest {
        // Given
        coEvery { mockApi.getCharacter(999) } throws HttpException(Response.error<Any>(404, "".toResponseBody(null)))

        // When
        val result = repository.getCharacter(999)

        // Then
        assertEquals(Result.Error(DataError.Network.NOT_FOUND), result)
    }

    // ---- getEpisodes ----

    @Test
    fun `given episode ids, when getEpisodes is called, then a single comma separated request is made and results are sorted by id`() = runTest {
        // Given
        coEvery { mockApi.getEpisodes("3,1") } returns listOf(
            EpisodeDto(id = 3, name = "Anatomy Park", episode = "S01E03"),
            EpisodeDto(id = 1, name = "Pilot", episode = "S01E01")
        )

        // When
        val result = repository.getEpisodes(listOf(3, 1))

        // Then
        assertTrue(result is Result.Success)
        assertEquals(listOf("Pilot", "Anatomy Park"), (result as Result.Success).data.map { it.name })
        coVerify(exactly = 1) { mockApi.getEpisodes(any()) }
    }

    @Test
    fun `given api throws IOException, when getEpisodes is called, then Result Error wraps NO_INTERNET`() = runTest {
        // Given
        coEvery { mockApi.getEpisodes(any()) } throws IOException("no network")

        // When
        val result = repository.getEpisodes(listOf(1))

        // Then
        assertEquals(Result.Error(DataError.Network.NO_INTERNET), result)
    }
}
