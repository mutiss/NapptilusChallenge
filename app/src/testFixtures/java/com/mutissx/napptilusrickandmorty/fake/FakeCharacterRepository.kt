package com.mutissx.napptilusrickandmorty.fake

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.domain.model.Episode
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class FakeCharacterRepository : CharacterRepository {

    val filtersReceived = mutableListOf<CharacterFilter>()
    val episodeRequests = mutableListOf<List<Int>>()

    var pagingSourceFactory: () -> FakeCharacterPagingSource =
        { FakeCharacterPagingSource(kotlin.Result.success(emptyList())) }

    var characterResult: Result<Character, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
    var episodesResult: Result<List<Episode>, DataError.Network> = Result.Success(emptyList())

    override fun getCharacters(filter: CharacterFilter): Flow<PagingData<Character>> {
        filtersReceived.add(filter)
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { pagingSourceFactory() }
        ).flow
    }

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> = characterResult

    override suspend fun getEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> {
        episodeRequests.add(ids)
        return episodesResult
    }
}
