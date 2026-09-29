package com.mutissx.napptilusrickandmorty.di

import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import com.mutissx.napptilusrickandmorty.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.napptilusrickandmorty.presentation.search.viewmodel.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {

    viewModel {
        SearchViewModel(searchCharactersUseCase = get())
    }

    viewModel {
        CharacterDetailViewModel(
            savedStateHandle = it.get(),
            getCharacterDetailUseCase = get(),
            getCharacterEpisodesUseCase = get(),
            toggleFavorite = get(),
            isFavoriteUseCase = get()
        )
    }

    viewModel {
        FavoritesViewModel(
            observeFavorites = get()
        )
    }
}
