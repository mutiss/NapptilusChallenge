package com.mutissx.napptilusrickandmorty.presentation.detail.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterDetailUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterEpisodesUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ObserveIsFavoriteUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.napptilusrickandmorty.fake.FakeCharacterRepository
import com.mutissx.napptilusrickandmorty.fake.FakeFavoritesRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import com.mutissx.napptilusrickandmorty.fake.anEpisode
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import com.mutissx.napptilusrickandmorty.presentation.navigation.Destination
import com.mutissx.napptilusrickandmorty.ui.theme.NapptilusRickAndMortyTheme
import com.mutissx.napptilusrickandmorty.util.waitForTag
import com.mutissx.napptilusrickandmorty.util.waitForText
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CharacterDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val characterId = 1
    private val rick = aCharacter(id = characterId, name = "Rick Sanchez", episodeIds = listOf(1, 2))
    private lateinit var fakeRepository: FakeCharacterRepository
    private lateinit var fakeFavoritesRepository: FakeFavoritesRepository
    private var backClicked = false

    @Before
    fun setUp() {
        fakeRepository = FakeCharacterRepository()
        fakeFavoritesRepository = FakeFavoritesRepository()
        backClicked = false
    }

    private fun setContent() {
        val viewModel = CharacterDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Destination.CharacterDetail.ARG_ID to characterId)),
            getCharacterDetailUseCase = GetCharacterDetailUseCase(fakeRepository, fakeFavoritesRepository),
            getCharacterEpisodesUseCase = GetCharacterEpisodesUseCase(fakeRepository),
            toggleFavorite = ToggleFavoriteUseCase(fakeFavoritesRepository),
            isFavoriteUseCase = ObserveIsFavoriteUseCase(fakeFavoritesRepository)
        )
        composeTestRule.setContent {
            NapptilusRickAndMortyTheme {
                CharacterDetailScreen(onBack = { backClicked = true }, viewModel = viewModel)
            }
        }
    }

    private fun scrollToTag(tag: String) {
        composeTestRule.onNodeWithTag(TestTags.DETAIL_CONTENT_LIST).performScrollToNode(hasTestTag(tag))
    }

    @Test
    fun given_character_and_episodes_load_when_screen_is_shown_then_name_info_and_episodes_are_displayed() {
        fakeRepository.characterResult = Result.Success(rick)
        fakeRepository.episodesResult = Result.Success(
            listOf(anEpisode(id = 1, name = "Pilot"), anEpisode(id = 2, name = "Lawnmower Dog", code = "S01E02"))
        )

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_CONTENT_LIST)

        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeTestRule.onNodeWithText("Earth (C-137)").assertIsDisplayed()
        scrollToTag(TestTags.DETAIL_EPISODE_ROW)
        composeTestRule.onAllNodesWithTag(TestTags.DETAIL_EPISODE_ROW).assertCountEquals(2)
    }

    @Test
    fun given_a_character_without_episodes_when_screen_is_shown_then_the_empty_episodes_message_is_displayed() {
        fakeRepository.characterResult = Result.Success(rick.copy(episodeIds = emptyList()))

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_EMPTY_EPISODES)
        scrollToTag(TestTags.DETAIL_EMPTY_EPISODES)

        composeTestRule.onNodeWithTag(TestTags.DETAIL_EMPTY_EPISODES).assertIsDisplayed()
    }

    @Test
    fun given_the_character_fetch_fails_when_retry_is_clicked_after_recovery_then_content_is_displayed() {
        fakeRepository.characterResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_ERROR_VIEW)

        fakeRepository.characterResult = Result.Success(rick)
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).performClick()
        composeTestRule.waitForTag(TestTags.DETAIL_CONTENT_LIST)

        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
    }

    @Test
    fun given_episodes_fail_when_retry_is_clicked_after_recovery_then_episodes_are_displayed_and_the_character_is_kept() {
        fakeRepository.characterResult = Result.Success(rick)
        fakeRepository.episodesResult = Result.Error(DataError.Network.SERVICE_UNAVAILABLE)

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_EPISODES_ERROR)

        fakeRepository.episodesResult = Result.Success(listOf(anEpisode(id = 1, name = "Pilot")))
        scrollToTag(TestTags.DETAIL_EPISODES_ERROR)
        composeTestRule.onNodeWithTag(TestTags.ERROR_RETRY_BUTTON).performClick()
        composeTestRule.waitForTag(TestTags.DETAIL_EPISODE_ROW)

        composeTestRule.onNodeWithText("Pilot").assertIsDisplayed()
    }

    @Test
    fun given_content_displayed_when_back_is_clicked_then_onBack_is_invoked() {
        fakeRepository.characterResult = Result.Success(rick)

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_CONTENT_LIST)
        composeTestRule.onNodeWithTag(TestTags.DETAIL_BACK_BUTTON).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun given_a_non_favorite_character_when_the_favorite_button_is_clicked_then_the_added_snackbar_is_shown() {
        fakeRepository.characterResult = Result.Success(rick)

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_FAVORITE_BUTTON)
        composeTestRule.onNodeWithTag(TestTags.DETAIL_FAVORITE_BUTTON).performClick()
        composeTestRule.waitForText("Rick Sanchez added to favorites")

        composeTestRule.onNodeWithText("Rick Sanchez added to favorites").assertIsDisplayed()
    }

    @Test
    fun given_a_favorite_character_when_the_favorite_button_is_clicked_then_the_removed_snackbar_is_shown() {
        fakeRepository.characterResult = Result.Success(rick)
        runBlocking { fakeFavoritesRepository.add(rick) }

        setContent()
        composeTestRule.waitForTag(TestTags.DETAIL_FAVORITE_BUTTON)
        composeTestRule.onNodeWithTag(TestTags.DETAIL_FAVORITE_BUTTON).performClick()
        composeTestRule.waitForText("Rick Sanchez removed from favorites")

        composeTestRule.onNodeWithText("Rick Sanchez removed from favorites").assertIsDisplayed()
    }
}
