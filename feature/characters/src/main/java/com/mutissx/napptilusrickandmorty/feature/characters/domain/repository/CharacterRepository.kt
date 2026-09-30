package com.mutissx.napptilusrickandmorty.feature.characters.domain.repository

import androidx.paging.PagingData
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getCharacters(filter: CharacterFilter): Flow<PagingData<Character>>
    suspend fun getCharacter(id: Int): Result<Character, DataError.Network>
    suspend fun getEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network>
}
