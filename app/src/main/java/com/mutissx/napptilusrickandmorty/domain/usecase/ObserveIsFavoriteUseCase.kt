package com.mutissx.napptilusrickandmorty.domain.usecase

import com.mutissx.napptilusrickandmorty.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveIsFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(id: Int): Flow<Boolean> =
        repository.observeIsFavorite(id)
}
