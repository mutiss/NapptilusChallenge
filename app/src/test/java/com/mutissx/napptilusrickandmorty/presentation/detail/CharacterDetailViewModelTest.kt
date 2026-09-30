package com.mutissx.napptilusrickandmorty.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterDetailUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterEpisodesUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ObserveIsFavoriteUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.napptilusrickandmorty.fake.FakeCharacterRepository
import com.mutissx.napptilusrickandmorty.fake.FakeFavoritesRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import com.mutissx.napptilusrickandmorty.fake.anEpisode
import com.mutissx.napptilusrickandmorty.presentation.detail.state.CharacterDetailUiState
import com.mutissx.napptilusrickandmorty.presentation.detail.state.EpisodesState
import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import com.mutissx.napptilusrickandmorty.presentation.navigation.Destination
import com.mutissx.napptilusrickandmorty.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeFavoritesRepository: FakeFavoritesRepository

    private val characterId = 1
    private val rick = aCharacter(id = characterId, episodeIds = listOf(1, 2))

    @Before
    fun setUp() {
        fakeRepository = FakeCharacterRepository()
        fakeFavoritesRepository = FakeFavoritesRepository()
    }

    private fun newViewModel(savedStateHandle: SavedStateHandle) = CharacterDetailViewModel(
        savedStateHandle = savedStateHandle,
        getCharacterDetailUseCase = GetCharacterDetailUseCase(fakeRepository, fakeFavoritesRepository),
        getCharacterEpisodesUseCase = GetCharacterEpisodesUseCase(fakeRepository),
        toggleFavorite = ToggleFavoriteUseCase(fakeFavoritesRepository),
        isFavoriteUseCase = ObserveIsFavoriteUseCase(fakeFavoritesRepository)
    )

    // uiState uses SharingStarted.WhileSubscribed, so it only computes while collected —
    // subscribe in the background so tests reading uiState.value see it update.
    private fun TestScope.createViewModel(): CharacterDetailViewModel =
        newViewModel(SavedStateHandle(mapOf(Destination.CharacterDetail.ARG_ID to characterId)))
            .also { viewModel -> backgroundScope.launch { viewModel.uiState.collect { } } }

    private val CharacterDetailViewModel.content: CharacterDetailUiState.Content
        get() = uiState.value as CharacterDetailUiState.Content

    @Test
    fun `given viewModel just created, when uiState is read before the load coroutines run, then state is Loading`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)

            // When
            val viewModel = createViewModel()

            // Then — StandardTestDispatcher hasn't run the load coroutine yet
            assertTrue(viewModel.uiState.value is CharacterDetailUiState.Loading)
        }

    @Test
    fun `given character and episodes load, when viewModel is created, then uiState is Content with episodes Loaded`() =
        runTest {
            // Given
            val episodes = listOf(anEpisode(id = 1), anEpisode(id = 2, code = "S01E02"))
            fakeRepository.characterResult = Result.Success(rick)
            fakeRepository.episodesResult = Result.Success(episodes)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(rick, viewModel.content.character)
            assertEquals(EpisodesState.Loaded(episodes), viewModel.content.episodes)
            assertEquals(listOf(listOf(1, 2)), fakeRepository.episodeRequests)
        }

    @Test
    fun `given the character fetch fails, when viewModel is created, then uiState is Error and episodes are never requested`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Error(DataError.Network.NOT_FOUND)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value as CharacterDetailUiState.Error
            assertEquals(R.string.not_found, (state.message as UiText.StringResource).resId)
            assertTrue(fakeRepository.episodeRequests.isEmpty())
        }

    @Test
    fun `given the character loads but episodes fail, when viewModel is created, then Content keeps the character with an episodes Error section`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            fakeRepository.episodesResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(rick, viewModel.content.character)
            val episodes = viewModel.content.episodes as EpisodesState.Error
            assertEquals(R.string.server_error, (episodes.message as UiText.StringResource).resId)
        }

    @Test
    fun `given an episodes Error section, when retryEpisodes is called after recovery, then episodes become Loaded`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            fakeRepository.episodesResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            val episodes = listOf(anEpisode())
            fakeRepository.episodesResult = Result.Success(episodes)
            viewModel.retryEpisodes()
            advanceUntilIdle()

            // Then
            assertEquals(rick, viewModel.content.character)
            assertEquals(EpisodesState.Loaded(episodes), viewModel.content.episodes)
        }

    @Test
    fun `given a failed initial load, when loadInfo is retried after recovery, then uiState becomes Content`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Error(DataError.Network.NO_INTERNET)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value is CharacterDetailUiState.Error)

            // When
            fakeRepository.characterResult = Result.Success(rick)
            viewModel.loadInfo()
            advanceUntilIdle()

            // Then
            assertEquals(rick, viewModel.content.character)
        }

    @Test(expected = IllegalArgumentException::class)
    fun `given a SavedStateHandle without the id argument, when viewModel is constructed, then it throws`() {
        newViewModel(SavedStateHandle())
    }

    // ---- favorites ----

    @Test
    fun `given a non favorite character, when onFavoriteToggle is called, then it is added and isFavorite becomes true`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertFalse(viewModel.content.isFavorite)

            // When
            viewModel.onFavoriteToggle()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(rick), fakeFavoritesRepository.addedCharacters)
            assertTrue(viewModel.content.isFavorite)
        }

    @Test
    fun `given a favorite character, when onFavoriteToggle is called, then it is removed and isFavorite becomes false`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            fakeFavoritesRepository.add(rick)
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.content.isFavorite)

            // When
            viewModel.onFavoriteToggle()
            advanceUntilIdle()

            // Then
            assertEquals(listOf(characterId), fakeFavoritesRepository.removedIds)
            assertFalse(viewModel.content.isFavorite)
        }

    @Test
    fun `given a non favorite character, when onFavoriteToggle is called, then the added message names the character`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.favoriteMessages.test {
                // When
                viewModel.onFavoriteToggle()

                // Then
                val message = awaitItem() as UiText.StringResource
                assertEquals(R.string.favorite_added, message.resId)
                assertEquals(rick.name, message.args.first())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `given the favorites write fails, when onFavoriteToggle is called, then an error message is emitted and isFavorite is unchanged`() =
        runTest {
            // Given
            fakeRepository.characterResult = Result.Success(rick)
            fakeFavoritesRepository.writeResult = Result.Error(DataError.Local.DISK_FULL)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.favoriteMessages.test {
                // When
                viewModel.onFavoriteToggle()

                // Then
                val message = awaitItem() as UiText.StringResource
                assertEquals(R.string.favorite_toggle_error, message.resId)
                cancelAndIgnoreRemainingEvents()
            }
            assertFalse(viewModel.content.isFavorite)
        }
}
