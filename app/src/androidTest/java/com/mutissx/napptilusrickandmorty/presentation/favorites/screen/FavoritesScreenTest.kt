package com.mutissx.napptilusrickandmorty.presentation.favorites.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mutissx.napptilusrickandmorty.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.napptilusrickandmorty.fake.FakeFavoritesRepository
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.napptilusrickandmorty.ui.theme.NapptilusRickAndMortyTheme
import com.mutissx.napptilusrickandmorty.util.waitForTag
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeFavoritesRepository
    private var clickedId: Int? = null

    @Before
    fun setUp() {
        fakeRepository = FakeFavoritesRepository()
        clickedId = null
    }

    private fun setContent() {
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(fakeRepository))
        composeTestRule.setContent {
            NapptilusRickAndMortyTheme {
                FavoritesScreen(
                    innerPadding = PaddingValues(),
                    onCharacterClick = { clickedId = it },
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun given_no_favorites_when_screen_is_shown_then_the_empty_view_is_displayed() {
        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_EMPTY_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_EMPTY_VIEW).assertIsDisplayed()
    }

    @Test
    fun given_favorites_exist_when_screen_is_shown_then_the_grid_displays_them() {
        runBlocking {
            fakeRepository.add(aCharacter(id = 1, name = "Rick Sanchez"))
            fakeRepository.add(aCharacter(id = 2, name = "Morty Smith"))
        }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_GRID)

        composeTestRule.onAllNodesWithTag(TestTags.FAVORITES_CHARACTER_CARD).assertCountEquals(2)
        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeTestRule.onNodeWithText("Morty Smith").assertIsDisplayed()
    }

    @Test
    fun given_favorites_exist_when_a_card_is_clicked_then_onCharacterClick_receives_its_id() {
        runBlocking { fakeRepository.add(aCharacter(id = 7, name = "Squanchy")) }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_GRID)
        composeTestRule.onNodeWithText("Squanchy").performClick()

        assertEquals(7, clickedId)
    }

    @Test
    fun given_the_screen_is_open_when_the_last_favorite_is_removed_then_the_empty_view_is_displayed() {
        runBlocking { fakeRepository.add(aCharacter(id = 1)) }

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_GRID)
        runBlocking { fakeRepository.remove(1) }
        composeTestRule.waitForTag(TestTags.FAVORITES_EMPTY_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_EMPTY_VIEW).assertIsDisplayed()
    }

    @Test
    fun given_the_repository_read_fails_when_screen_is_shown_then_the_error_view_is_displayed() {
        fakeRepository.readError = RuntimeException("disk read failure")

        setContent()
        composeTestRule.waitForTag(TestTags.FAVORITES_ERROR_VIEW)

        composeTestRule.onNodeWithTag(TestTags.FAVORITES_ERROR_VIEW).assertIsDisplayed()
    }
}
