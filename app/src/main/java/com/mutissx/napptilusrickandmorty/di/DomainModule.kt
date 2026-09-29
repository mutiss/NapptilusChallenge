package com.mutissx.napptilusrickandmorty.di

import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterDetailUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.GetCharacterEpisodesUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ObserveFavoritesUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ObserveIsFavoriteUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.SearchCharactersUseCase
import com.mutissx.napptilusrickandmorty.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SearchCharactersUseCase(repository = get()) }

    factory { GetCharacterDetailUseCase(characterRepository = get(), favoritesRepository = get()) }

    factory { GetCharacterEpisodesUseCase(repository = get()) }

    factory { ObserveIsFavoriteUseCase(repository = get()) }

    factory { ObserveFavoritesUseCase(repository = get()) }

    factory { ToggleFavoriteUseCase(repository = get()) }
}
