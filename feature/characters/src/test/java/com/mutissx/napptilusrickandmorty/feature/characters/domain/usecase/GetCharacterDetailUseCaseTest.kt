package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.CharacterRepository
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository
import com.mutissx.napptilusrickandmorty.feature.characters.fake.aCharacter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCharacterDetailUseCaseTest {

    private val characterRepository: CharacterRepository = mockk()
    private val favoritesRepository: FavoritesRepository = mockk()
    private val useCase = GetCharacterDetailUseCase(characterRepository, favoritesRepository)

    @Test
    fun `given the character repository succeeds, when invoke is called, then its result is returned without reading favorites`() = runTest {
        // Given
        val character = aCharacter(id = 1)
        coEvery { characterRepository.getCharacter(1) } returns Result.Success(character)

        // When
        val result = useCase(1)

        // Then
        assertEquals(Result.Success(character), result)
        coVerify(exactly = 0) { favoritesRepository.getFavorite(any()) }
    }

    @Test
    fun `given the network fails but the character is a saved favorite, when invoke is called, then the local snapshot is returned`() = runTest {
        // Given
        val saved = aCharacter(id = 1, name = "Rick (saved)")
        coEvery { characterRepository.getCharacter(1) } returns Result.Error(DataError.Network.NO_INTERNET)
        coEvery { favoritesRepository.getFavorite(1) } returns Result.Success(saved)

        // When / Then
        assertEquals(Result.Success(saved), useCase(1))
    }

    @Test
    fun `given the network fails and the character is not a favorite, when invoke is called, then the network error is returned`() = runTest {
        // Given
        coEvery { characterRepository.getCharacter(1) } returns Result.Error(DataError.Network.NO_INTERNET)
        coEvery { favoritesRepository.getFavorite(1) } returns Result.Success(null)

        // When / Then
        assertEquals(Result.Error(DataError.Network.NO_INTERNET), useCase(1))
    }

    @Test
    fun `given both the network and the local read fail, when invoke is called, then the network error is returned`() = runTest {
        // Given
        coEvery { characterRepository.getCharacter(1) } returns Result.Error(DataError.Network.REQUEST_TIMEOUT)
        coEvery { favoritesRepository.getFavorite(1) } returns Result.Error(DataError.Local.UNKNOWN)

        // When / Then
        assertEquals(Result.Error(DataError.Network.REQUEST_TIMEOUT), useCase(1))
    }
}
