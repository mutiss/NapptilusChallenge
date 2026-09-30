package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(character: Character, isCurrentlyFavorite: Boolean): Result<Unit, DataError.Local> =
        if (isCurrentlyFavorite) {
            repository.remove(character.id)
        } else {
            repository.add(character)
        }
}
