package com.mutissx.napptilusrickandmorty.presentation.favorites.screen

import com.mutissx.napptilusrickandmorty.domain.model.Character

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Error : FavoritesUiState
    data class Content(val favorites: List<Character>) : FavoritesUiState
}
