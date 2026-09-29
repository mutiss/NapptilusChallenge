package com.mutissx.napptilusrickandmorty.data.repository

import app.cash.turbine.test
import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.data.local.FavoriteCharacterDao
import com.mutissx.napptilusrickandmorty.data.mapper.toEntity
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FavoritesRepositoryImplTest {

    private val mockDao: FavoriteCharacterDao = mockk()
    private lateinit var repository: FavoritesRepositoryImpl

    private val fixedNow = 1_700_000_000_000L

    @Before
    fun setUp() {
        repository = FavoritesRepositoryImpl(mockDao, clock = { fixedNow })
    }

    @Test
    fun `given dao returns entities, when observeAll is collected, then it emits the mapped domain characters`() = runTest {
        // Given
        val character = aCharacter(id = 1)
        every { mockDao.observeAll() } returns flowOf(listOf(character.toEntity(addedAt = 1L)))

        // When / Then
        repository.observeAll().test {
            assertEquals(listOf(character), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given dao returns no entities, when observeAll is collected, then it emits an empty list`() = runTest {
        // Given
        every { mockDao.observeAll() } returns flowOf(emptyList())

        // When / Then
        repository.observeAll().test {
            assertEquals(emptyList<Character>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given dao emits a favorite status, when observeIsFavorite is collected, then it delegates to the dao unchanged`() = runTest {
        // Given
        every { mockDao.observeIsFavorite(1) } returns flowOf(true)

        // When / Then
        repository.observeIsFavorite(1).test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given a stored favorite, when getFavorite is called, then the mapped character is returned`() = runTest {
        // Given
        val character = aCharacter(id = 1)
        coEvery { mockDao.getById(1) } returns character.toEntity(addedAt = 1L)

        // When / Then
        assertEquals(Result.Success(character), repository.getFavorite(1))
    }

    @Test
    fun `given no stored favorite, when getFavorite is called, then Success with null is returned`() = runTest {
        // Given
        coEvery { mockDao.getById(1) } returns null

        // When / Then
        assertEquals(Result.Success(null), repository.getFavorite(1))
    }

    @Test
    fun `given a character, when add is called, then the dao inserts an entity stamped with the clock time and returns Success`() = runTest {
        // Given
        val character = aCharacter(id = 1)
        coEvery { mockDao.insert(any()) } returns Unit

        // When
        val result = repository.add(character)

        // Then
        assertEquals(Result.Success(Unit), result)
        coVerify { mockDao.insert(character.toEntity(addedAt = fixedNow)) }
    }

    @Test
    fun `given an id, when remove is called, then the dao deletes that id and returns Success`() = runTest {
        // Given
        coEvery { mockDao.delete(1) } returns Unit

        // When
        val result = repository.remove(1)

        // Then
        assertEquals(Result.Success(Unit), result)
        coVerify { mockDao.delete(1) }
    }

    @Test
    fun `given the dao throws on insert, when add is called, then it returns Result Error instead of propagating`() = runTest {
        // Given
        coEvery { mockDao.insert(any()) } throws RuntimeException("disk failure")

        // When
        val result = repository.add(aCharacter())

        // Then
        assertEquals(Result.Error(DataError.Local.UNKNOWN), result)
    }

    @Test
    fun `given the dao throws on delete, when remove is called, then it returns Result Error instead of propagating`() = runTest {
        // Given
        coEvery { mockDao.delete(1) } throws RuntimeException("disk failure")

        // When
        val result = repository.remove(1)

        // Then
        assertEquals(Result.Error(DataError.Local.UNKNOWN), result)
    }
}
