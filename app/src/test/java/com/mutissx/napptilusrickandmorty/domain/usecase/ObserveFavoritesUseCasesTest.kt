package com.mutissx.napptilusrickandmorty.domain.usecase

import app.cash.turbine.test
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.fake.FakeFavoritesRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Covers both observe use cases against a stateful fake, since they only make sense reactively. */
class ObserveFavoritesUseCasesTest {

    private val repository = FakeFavoritesRepository()
    private val observeFavorites = ObserveFavoritesUseCase(repository)
    private val observeIsFavorite = ObserveIsFavoriteUseCase(repository)

    @Test
    fun `given favorites change, when observeFavorites is collected, then every change is emitted`() = runTest {
        val rick = aCharacter(id = 1)

        observeFavorites().test {
            assertEquals(emptyList<Character>(), awaitItem())

            repository.add(rick)
            assertEquals(listOf(rick), awaitItem())

            repository.remove(rick.id)
            assertEquals(emptyList<Character>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given the character becomes a favorite, when observeIsFavorite is collected, then it flips to true`() = runTest {
        observeIsFavorite(1).test {
            assertFalse(awaitItem())

            repository.add(aCharacter(id = 1))

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given another character becomes a favorite, when observeIsFavorite is collected, then it stays false`() = runTest {
        observeIsFavorite(1).test {
            assertFalse(awaitItem())

            repository.add(aCharacter(id = 2))

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
