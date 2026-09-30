package com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.SearchCharactersUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeCharacterPagingSource
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeCharacterRepository
import com.mutissx.napptilusrickandmorty.feature.characters.fake.FakeConnectivityObserver
import com.mutissx.napptilusrickandmorty.feature.characters.fake.aCharacter
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.viewmodel.SearchViewModel
import com.mutissx.napptilusrickandmorty.core.ui.theme.NapptilusRickAndMortyTheme
import com.mutissx.napptilusrickandmorty.feature.characters.util.waitForTag
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeConnectivity: FakeConnectivityObserver
    private var clickedId: Int? = null

    @Before
    fun setUp() {
        fakeRepository = FakeCharacterRepository()
        fakeConnectivity = FakeConnectivityObserver()
        clickedId = null
    }

    private fun setContent() {
        val viewModel = SearchViewModel(
            SearchCharactersUseCase(fakeRepository),
            fakeConnectivity
        )
        composeTestRule.setContent {
            NapptilusRickAndMortyTheme {
                SearchScreen(
                    innerPadding = PaddingValues(),
                    onCharacterClick = { clickedId = it },
                    viewModel = viewModel
                )
            }
        }
    }

    private fun returning(characters: List<Character>) {
        fakeRepository.pagingSourceFactory = { FakeCharacterPagingSource(Result.success(characters)) }
    }

    @Test
    fun given_characters_available_when_screen_opens_then_the_grid_lists_them_without_typing() {
        returning(listOf(aCharacter(id = 1, name = "Rick Sanchez"), aCharacter(id = 2, name = "Morty Smith")))

        setContent()
        composeTestRule.waitForTag(TestTags.CHARACTER_GRID)

        composeTestRule.onAllNodesWithTag(TestTags.CHARACTER_CARD).assertCountEquals(2)
        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeTestRule.onNodeWithText("Morty Smith").assertIsDisplayed()
    }

    @Test
    fun given_a_query_with_no_matches_when_debounce_elapses_then_the_no_results_view_is_displayed() {
        setContent()

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("zzz")
        composeTestRule.waitForTag(TestTags.EMPTY_VIEW_NO_RESULTS)

        composeTestRule.onNodeWithTag(TestTags.EMPTY_VIEW_NO_RESULTS).assertIsDisplayed()
    }

    @Test
    fun given_a_repository_error_when_screen_opens_then_the_error_view_with_retry_is_displayed() {
        fakeRepository.pagingSourceFactory = { FakeCharacterPagingSource(Result.failure(RuntimeException("boom"))) }

        setContent()
        composeTestRule.waitForTag(TestTags.ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.ERROR_VIEW).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).assertIsDisplayed()
    }

    @Test
    fun given_an_error_state_when_retry_is_clicked_then_the_grid_is_displayed_after_a_successful_retry() {
        val results = ArrayDeque(
            listOf(
                Result.failure<List<Character>>(RuntimeException("boom")),
                Result.success(listOf(aCharacter(id = 1, name = "Birdperson")))
            )
        )
        fakeRepository.pagingSourceFactory = { FakeCharacterPagingSource { results.removeFirst() } }

        setContent()
        composeTestRule.waitForTag(TestTags.ERROR_VIEW)
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).performClick()
        composeTestRule.waitForTag(TestTags.CHARACTER_GRID)

        composeTestRule.onNodeWithText("Birdperson").assertIsDisplayed()
    }

    @Test
    fun given_results_on_screen_when_a_new_filter_fails_then_the_error_replaces_the_stale_results() {
        val results = ArrayDeque(
            listOf(
                Result.success(listOf(aCharacter(id = 1, name = "Rick Sanchez"))),
                Result.failure<List<Character>>(RuntimeException("offline"))
            )
        )
        fakeRepository.pagingSourceFactory = { FakeCharacterPagingSource { results.removeFirst() } }

        setContent()
        composeTestRule.waitForTag(TestTags.CHARACTER_GRID)
        composeTestRule.onNodeWithTag(TestTags.statusChip(CharacterStatus.DEAD)).performClick()
        composeTestRule.waitForTag(TestTags.ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(TestTags.CHARACTER_CARD).assertCountEquals(0)
    }

    @Test
    fun given_results_on_screen_when_the_device_goes_offline_then_results_stay_and_search_and_filters_are_disabled() {
        returning(listOf(aCharacter(id = 1, name = "Rick Sanchez")))
        setContent()
        composeTestRule.waitForTag(TestTags.CHARACTER_GRID)

        fakeConnectivity.setOnline(false)
        composeTestRule.waitForTag(TestTags.OFFLINE_BANNER)

        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.statusChip(CharacterStatus.DEAD)).assertIsNotEnabled()
        val filtersBefore = fakeRepository.filtersReceived.size
        composeTestRule.onNodeWithTag(TestTags.statusChip(CharacterStatus.DEAD)).performClick()
        composeTestRule.waitForIdle()
        assertEquals(filtersBefore, fakeRepository.filtersReceived.size)
    }

    @Test
    fun given_the_device_is_offline_when_it_reconnects_then_the_banner_hides_and_filters_are_enabled_again() {
        fakeConnectivity.setOnline(false)
        setContent()
        composeTestRule.waitForTag(TestTags.OFFLINE_BANNER)

        fakeConnectivity.setOnline(true)
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodesWithTag(TestTags.OFFLINE_BANNER).fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).assertIsEnabled()
        composeTestRule.onNodeWithTag(TestTags.statusChip(CharacterStatus.DEAD)).assertIsEnabled()
    }

    @Test
    fun given_a_status_chip_when_it_is_clicked_then_the_repository_is_queried_with_that_status() {
        setContent()
        composeTestRule.waitForTag(TestTags.EMPTY_VIEW_NO_RESULTS)

        composeTestRule.onNodeWithTag(TestTags.statusChip(CharacterStatus.DEAD)).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            fakeRepository.filtersReceived.lastOrNull() == CharacterFilter(status = CharacterStatus.DEAD)
        }
    }

    @Test
    fun given_a_typed_query_when_the_clear_button_is_clicked_then_the_unfiltered_list_is_requested_again() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SEARCH_TEXT_FIELD).performTextInput("rick")
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            fakeRepository.filtersReceived.lastOrNull() == CharacterFilter(name = "rick")
        }

        composeTestRule.onNodeWithTag(TestTags.SEARCH_CLEAR_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            fakeRepository.filtersReceived.lastOrNull() == CharacterFilter()
        }
    }

    @Test
    fun given_characters_displayed_when_a_card_is_clicked_then_onCharacterClick_receives_its_id() {
        returning(listOf(aCharacter(id = 42, name = "Mr. Poopybutthole")))

        setContent()
        composeTestRule.waitForTag(TestTags.CHARACTER_GRID)
        composeTestRule.onNodeWithText("Mr. Poopybutthole").performClick()

        assertEquals(42, clickedId)
    }
}
