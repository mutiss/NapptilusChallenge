package com.mutissx.napptilusrickandmorty.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mutissx.napptilusrickandmorty.core.data.safeApiCall
import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.data.mapper.toDomain
import com.mutissx.napptilusrickandmorty.data.paging.CharacterPagingSource
import com.mutissx.napptilusrickandmorty.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.domain.model.Episode
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class CharacterRepositoryImpl(
    private val api: RickAndMortyApi,
    private val memoryCache: CharacterMemoryCache
) : CharacterRepository {

    override fun getCharacters(filter: CharacterFilter): Flow<PagingData<Character>> =
        Pager(
            config = PagingConfig(
                pageSize = CharacterPagingSource.PAGE_SIZE,
                initialLoadSize = CharacterPagingSource.PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE, //Remove if we want smoother infinite scroll
                enablePlaceholders = false
            ),
            pagingSourceFactory = { CharacterPagingSource(api, filter, memoryCache) }
        ).flow

    override suspend fun getCharacter(id: Int): Result<Character, DataError.Network> =
        memoryCache.get(id)?.let { Result.Success(it) }
            ?: safeApiCall { api.getCharacter(id).toDomain().also(memoryCache::put) }

    override suspend fun getEpisodes(ids: List<Int>): Result<List<Episode>, DataError.Network> =
        safeApiCall {
            api.getEpisodes(ids.joinToString(","))
                .map { it.toDomain() }
                .sortedBy { it.id }
        }

    companion object {
        private const val PREFETCH_DISTANCE = 2
    }
}
