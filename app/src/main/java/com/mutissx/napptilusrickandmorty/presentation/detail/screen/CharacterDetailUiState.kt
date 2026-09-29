package com.mutissx.napptilusrickandmorty.presentation.detail.screen

import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.Episode

sealed interface CharacterDetailUiState {
    data object Loading : CharacterDetailUiState
    data class Error(val message: UiText) : CharacterDetailUiState
    data class Content(
        val character: Character,
        val isFavorite: Boolean,
        val episodes: EpisodesState
    ) : CharacterDetailUiState
}

sealed interface EpisodesState {
    data object Loading : EpisodesState
    data class Loaded(val items: List<Episode>) : EpisodesState
    data class Error(val message: UiText) : EpisodesState
}

sealed interface CharacterSectionState {
    data object Loading : CharacterSectionState
    data class Error(val message: UiText) : CharacterSectionState
    data class Loaded(val data: Character) : CharacterSectionState
}
