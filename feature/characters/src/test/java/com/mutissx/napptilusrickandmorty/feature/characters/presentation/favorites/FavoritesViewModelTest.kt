package com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites

import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeFavoritesRepository
import com.mutissx.napptilusrickandmorty.feature.characters.fake.aCharacter
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.screen.FavoritesUiState
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.napptilusrickandmorty.feature.characters.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeFavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        viewModel = FavoritesViewModel(ObserveFavoritesUseCase(fakeRepository))
    }

    private val FavoritesViewModel.favorites
        get() = (uiState.value as FavoritesUiState.Content).favorites

    @Test
    fun `given viewModel just created, when uiState is read before collection starts, then it is Loading`() {
        assertTrue(viewModel.uiState.value is FavoritesUiState.Loading)
    }

    @Test
    fun `given no favorites, when uiState is collected, then it emits Content with an empty list`() = runTest {
        // When
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.favorites.isEmpty())
    }

    @Test
    fun `given uiState is being collected, when favorites are added and removed, then uiState follows`() = runTest {
        // Given
        val rick = aCharacter(id = 1)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        // When — added
        fakeRepository.add(rick)
        advanceUntilIdle()

        // Then
        assertEquals(listOf(rick), viewModel.favorites)

        // When — removed
        fakeRepository.remove(rick.id)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.favorites.isEmpty())
    }

    @Test
    fun `given the repository read fails, when uiState is collected, then it emits Error instead of crashing`() = runTest {
        // Given — readError must be set before the ViewModel builds its pipeline
        val failingViewModel = FavoritesViewModel(
            ObserveFavoritesUseCase(FakeFavoritesRepository().apply { readError = RuntimeException("disk read failure") })
        )

        // When
        backgroundScope.launch { failingViewModel.uiState.collect { } }
        advanceUntilIdle()

        // Then
        assertTrue(failingViewModel.uiState.value is FavoritesUiState.Error)
    }
}
