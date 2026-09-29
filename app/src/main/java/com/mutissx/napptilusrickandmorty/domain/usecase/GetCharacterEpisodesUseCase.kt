package com.mutissx.napptilusrickandmorty.domain.usecase

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.Episode
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository

class GetCharacterEpisodesUseCase(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(character: Character): Result<List<Episode>, DataError.Network> =
        if (character.episodeIds.isEmpty()) {
            Result.Success(emptyList())
        } else {
            repository.getEpisodes(character.episodeIds)
        }
}
