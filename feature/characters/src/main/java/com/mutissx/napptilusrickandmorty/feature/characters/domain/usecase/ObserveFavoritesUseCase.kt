package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(
    private val repository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<Character>> = repository.observeAll()
}
