package com.mutissx.napptilusrickandmorty.domain.usecase

import androidx.paging.PagingData
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class SearchCharactersUseCase(
    private val repository: CharacterRepository
) {
    operator fun invoke(filter: CharacterFilter): Flow<PagingData<Character>> =
        repository.getCharacters(filter)
}
