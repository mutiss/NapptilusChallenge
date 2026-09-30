package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveIsFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> =
        repository.observeIsFavorite(id)
}
