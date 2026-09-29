package com.mutissx.napptilusrickandmorty.domain.usecase

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.repository.FavoritesRepository

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
