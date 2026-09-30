package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Episode
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.CharacterRepository

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
