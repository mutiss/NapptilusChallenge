package com.mutissx.napptilusrickandmorty.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mutissx.napptilusrickandmorty.core.data.toNetworkError
import com.mutissx.napptilusrickandmorty.core.domain.DataException
import com.mutissx.napptilusrickandmorty.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.data.mapper.toDomain
import com.mutissx.napptilusrickandmorty.data.mapper.toQueryParam
import com.mutissx.napptilusrickandmorty.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterFilter
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class CharacterPagingSource(
    private val api: RickAndMortyApi,
    private val filter: CharacterFilter,
    private val memoryCache: CharacterMemoryCache
) : PagingSource<Int, Character>() {

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.getCharacters(
                page = page,
                name = filter.name.takeIf { it.isNotBlank() },
                status = filter.status?.toQueryParam(),
                gender = filter.gender?.toQueryParam()
            )
            val characters = response.results.map { it.toDomain() }
            memoryCache.putAll(characters)
            LoadResult.Page(
                data = characters,
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = if (response.info.next == null) null else page + 1
            )
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            // The API answers a search with no matches with a 404 instead of an empty list:
            // that's a valid, empty result — not an error to show the user.
            if (t is HttpException && t.code() == HTTP_NOT_FOUND) {
                LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            } else {
                LoadResult.Error(DataException(t.toNetworkError()))
            }
        }
    }

    companion object {
        const val FIRST_PAGE = 1

        /** Fixed server-side by the Rick and Morty API; it can't be requested. */
        const val PAGE_SIZE = 20
        private const val HTTP_NOT_FOUND = 404
    }
}
