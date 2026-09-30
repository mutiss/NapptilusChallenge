package com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.viewmodel

import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.core.ui.R as CoreUiR
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.DataException
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.SearchCharactersUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeCharacterRepository
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeConnectivityObserver
import com.mutissx.napptilusrickandmorty.feature.characters.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeConnectivity: FakeConnectivityObserver
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        fakeRepository = FakeCharacterRepository()
        fakeConnectivity = FakeConnectivityObserver()
        viewModel = SearchViewModel(
            SearchCharactersUseCase(fakeRepository),
            fakeConnectivity
        )
    }

    // cachedIn (Paging 3) is lazy: it only collects upstream while someone collects `results`.
    // Each test starts a background collector to activate the debounce/combine pipeline, then
    // asserts on the filters the fake repository received. The collector subscribes eagerly
    // (Unconfined): advanceUntilIdle() only drains foreground work, so a queued background
    // subscription would otherwise start late and miss the initial emission.
    private fun TestScope.startCollectingResults() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.results.collect { } }
        advanceUntilIdle()
    }

    // ---- initial state ----

    @Test
    fun `given a new viewModel, when results are collected, then every character is requested with no filter`() =
        runTest {
            // When
            startCollectingResults()

            // Then
            assertEquals(listOf(CharacterFilter()), fakeRepository.filtersReceived)
            assertEquals(SearchViewModel.EMPTY_VALUE, viewModel.query.value)
            assertNull(viewModel.status.value)
            assertNull(viewModel.gender.value)
        }

    // ---- query ----

    @Test
    fun `given a typed query, when the debounce window elapses, then the repository receives the trimmed name`() =
        runTest {
            // Given
            startCollectingResults()

            // When
            viewModel.onQueryChange("  rick  ")
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS + 1)

            // Then
            assertEquals(CharacterFilter(name = "rick"), fakeRepository.filtersReceived.last())
        }

    @Test
    fun `given a typed query, when the debounce window has not elapsed yet, then the repository is not called again`() =
        runTest {
            // Given
            startCollectingResults()

            // When
            viewModel.onQueryChange("rick")
            advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS - 100)

            // Then — only the initial unfiltered request so far
            assertEquals(1, fakeRepository.filtersReceived.size)
        }

    @Test
    fun `given two rapid changes within the debounce window, when it elapses, then only the last query is searched`() =
        runTest {
            // Given
            startCollectingResults()

            // When
            viewModel.onQueryChange("mo")
            advanceTimeBy(200L)
            viewModel.onQueryChange("morty")
            advanceUntilIdle()

            // Then
            assertEquals(
                listOf(CharacterFilter(), CharacterFilter(name = "morty")),
                fakeRepository.filtersReceived
            )
        }

    @Test
    fun `given a query that only differs by whitespace, when it settles, then no duplicate search is made`() =
        runTest {
            // Given
            startCollectingResults()
            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            // When
            viewModel.onQueryChange("rick ")
            advanceUntilIdle()

            // Then
            assertEquals(2, fakeRepository.filtersReceived.size)
        }

    @Test
    fun `given an active query, when onClearQuery is called, then the unfiltered list is requested immediately`() =
        runTest {
            // Given
            startCollectingResults()
            viewModel.onQueryChange("rick")
            advanceUntilIdle()

            // When
            viewModel.onClearQuery()
            advanceTimeBy(1L)

            // Then — clearing is not debounced
            assertEquals(SearchViewModel.EMPTY_VALUE, viewModel.query.value)
            assertEquals(CharacterFilter(), fakeRepository.filtersReceived.last())
        }

    // ---- filters ----

    @Test
    fun `given a status chip is tapped, when results are collected, then the status filter is applied without debounce`() =
        runTest {
            // Given
            startCollectingResults()

            // When
            viewModel.onStatusSelected(CharacterStatus.DEAD)
            advanceTimeBy(1L)

            // Then
            assertEquals(CharacterStatus.DEAD, viewModel.status.value)
            assertEquals(CharacterFilter(status = CharacterStatus.DEAD), fakeRepository.filtersReceived.last())
        }

    @Test
    fun `given a selected status, when the same chip is tapped again, then the status filter is cleared`() =
        runTest {
            // Given
            startCollectingResults()
            viewModel.onStatusSelected(CharacterStatus.ALIVE)
            advanceUntilIdle()

            // When
            viewModel.onStatusSelected(CharacterStatus.ALIVE)
            advanceUntilIdle()

            // Then
            assertNull(viewModel.status.value)
            assertEquals(CharacterFilter(), fakeRepository.filtersReceived.last())
        }

    @Test
    fun `given a query, a status and a gender, when all settle, then the repository receives them combined`() =
        runTest {
            // Given
            startCollectingResults()

            // When
            viewModel.onQueryChange("smith")
            viewModel.onStatusSelected(CharacterStatus.ALIVE)
            viewModel.onGenderSelected(CharacterGender.FEMALE)
            advanceUntilIdle()

            // Then
            assertEquals(
                CharacterFilter(name = "smith", status = CharacterStatus.ALIVE, gender = CharacterGender.FEMALE),
                fakeRepository.filtersReceived.last()
            )
        }

    @Test
    fun `given a selected gender, when another gender is tapped, then the selection is replaced`() =
        runTest {
            // Given
            startCollectingResults()
            viewModel.onGenderSelected(CharacterGender.MALE)

            // When
            viewModel.onGenderSelected(CharacterGender.GENDERLESS)
            advanceUntilIdle()

            // Then
            assertEquals(CharacterGender.GENDERLESS, viewModel.gender.value)
        }

    // ---- connectivity ----

    @Test
    fun `given the device goes offline, when isOnline is collected, then it reports false`() =
        runTest {
            // Given
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isOnline.collect { } }

            // When
            fakeConnectivity.setOnline(false)
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.isOnline.value)
        }

    // ---- errorMessage ----

    @Test
    fun `given a DataException wrapping a network error, when errorMessage is called, then the wrapped error's UiText is returned`() {
        val uiText = viewModel.errorMessage(DataException(DataError.Network.NO_INTERNET))

        assertEquals(CoreUiR.string.no_internet, (uiText as UiText.StringResource).resId)
    }

    @Test
    fun `given a throwable that is not a DataException, when errorMessage is called, then the generic search error UiText is returned`() {
        val uiText = viewModel.errorMessage(RuntimeException("unexpected"))

        assertEquals(R.string.search_error_generic, (uiText as UiText.StringResource).resId)
    }
}
