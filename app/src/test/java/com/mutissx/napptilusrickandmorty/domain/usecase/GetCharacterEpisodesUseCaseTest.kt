package com.mutissx.napptilusrickandmorty.domain.usecase

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Episode
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import com.mutissx.napptilusrickandmorty.fake.anEpisode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCharacterEpisodesUseCaseTest {

    private val repository: CharacterRepository = mockk()
    private val useCase = GetCharacterEpisodesUseCase(repository)

    @Test
    fun `given a character with episodes, when invoke is called, then the repository is asked for exactly those ids`() = runTest {
        // Given
        val episodes = listOf(anEpisode(id = 1), anEpisode(id = 2, name = "Lawnmower Dog", code = "S01E02"))
        coEvery { repository.getEpisodes(listOf(1, 2)) } returns Result.Success(episodes)

        // When
        val result = useCase(aCharacter(episodeIds = listOf(1, 2)))

        // Then
        assertEquals(Result.Success(episodes), result)
    }

    @Test
    fun `given a character without episodes, when invoke is called, then an empty list is returned without hitting the repository`() = runTest {
        // When
        val result = useCase(aCharacter(episodeIds = emptyList()))

        // Then
        assertEquals(Result.Success(emptyList<Episode>()), result)
        coVerify(exactly = 0) { repository.getEpisodes(any()) }
    }

    @Test
    fun `given the repository fails, when invoke is called, then the error is propagated`() = runTest {
        // Given
        coEvery { repository.getEpisodes(any()) } returns Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

        // When / Then
        assertEquals(Result.Error(DataError.Network.SERVICE_UNAVAILABLE), useCase(aCharacter()))
    }
}
