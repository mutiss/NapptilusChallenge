package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.CharacterRepository
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository

/**
 * Fresh data first; if it can't be fetched (e.g. offline), a character saved as favourite is
 * still viewable from its local snapshot. Only when neither is available is the original
 * network error reported.
 */
class GetCharacterDetailUseCase(
    private val characterRepository: CharacterRepository,
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(id: Int): Result<Character, DataError.Network> {
        val remote = characterRepository.getCharacter(id)
        if (remote is Result.Success) return remote

        val savedFavorite = (favoritesRepository.getFavorite(id) as? Result.Success)?.data
        return savedFavorite?.let { Result.Success(it) } ?: remote
    }
}
