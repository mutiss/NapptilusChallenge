package com.mutissx.napptilusrickandmorty.feature.characters.presentation.components

import com.mutissx.napptilusrickandmorty.core.ui.components.CoreTestTags
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus

object TestTags {
    const val SEARCH_TEXT_FIELD = "search_text_field"
    const val SEARCH_CLEAR_BUTTON = "search_clear_button"
    const val CHARACTER_GRID = "character_grid"
    const val CHARACTER_CARD = "character_card"
    const val INITIAL_LOADING_INDICATOR = "initial_loading_indicator"
    const val APPEND_LOADING_INDICATOR = "append_loading_indicator"
    const val APPEND_ERROR_VIEW = "append_error_view"
    const val ERROR_VIEW = "error_view"
    const val ERROR_RETRY_BUTTON = CoreTestTags.ERROR_RETRY_BUTTON
    const val EMPTY_VIEW_NO_RESULTS = "empty_view_no_results"
    const val OFFLINE_BANNER = "offline_banner"

    const val DETAIL_BACK_BUTTON = "detail_back_button"
    const val DETAIL_LOADING_INDICATOR = "detail_loading_indicator"
    const val DETAIL_ERROR_VIEW = "detail_error_view"
    const val DETAIL_CONTENT_LIST = "detail_content_list"
    const val DETAIL_EPISODE_ROW = "detail_episode_row"
    const val DETAIL_EMPTY_EPISODES = "detail_empty_episodes"
    const val DETAIL_EPISODES_LOADING = "detail_episodes_loading"
    const val DETAIL_EPISODES_ERROR = "detail_episodes_error"
    const val DETAIL_FAVORITE_BUTTON = "detail_favorite_button"

    const val FAVORITES_LOADING_INDICATOR = "favorites_loading_indicator"
    const val FAVORITES_EMPTY_VIEW = "favorites_empty_view"
    const val FAVORITES_ERROR_VIEW = "favorites_error_view"
    const val FAVORITES_GRID = "favorites_grid"
    const val FAVORITES_CHARACTER_CARD = "favorites_character_card"

    fun statusChip(status: CharacterStatus) = "status_chip_${status.name}"
    fun genderChip(gender: CharacterGender) = "gender_chip_${gender.name}"
}
