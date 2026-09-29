package com.mutissx.napptilusrickandmorty.domain.usecase

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.repository.FavoritesRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val repository: FavoritesRepository = mockk()
    private val useCase = ToggleFavoriteUseCase(repository)

    private val character = aCharacter(id = 1)

    @Test
    fun `given the character is not currently favorite, when invoke is called, then it is added`() = runTest {
        // Given
        coEvery { repository.add(character) } returns Result.Success(Unit)

        // When
        useCase(character, isCurrentlyFavorite = false)

        // Then
        coVerify(exactly = 1) { repository.add(character) }
        coVerify(exactly = 0) { repository.remove(any()) }
    }

    @Test
    fun `given the character is currently favorite, when invoke is called, then it is removed by id`() = runTest {
        // Given
        coEvery { repository.remove(1) } returns Result.Success(Unit)

        // When
        useCase(character, isCurrentlyFavorite = true)

        // Then
        coVerify(exactly = 1) { repository.remove(1) }
        coVerify(exactly = 0) { repository.add(any()) }
    }

    @Test
    fun `given the repository write fails, when invoke is called, then the failure Result is returned`() = runTest {
        // Given
        coEvery { repository.add(character) } returns Result.Error(DataError.Local.DISK_FULL)

        // When / Then
        assertEquals(Result.Error(DataError.Local.DISK_FULL), useCase(character, isCurrentlyFavorite = false))
    }
}
