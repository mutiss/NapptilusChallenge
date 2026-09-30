package com.mutissx.napptilusrickandmorty.feature.characters.fake

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character

class FakeCharacterPagingSource(
    private val resultProvider: () -> Result<List<Character>>
) : PagingSource<Int, Character>() {

    constructor(result: Result<List<Character>>) : this({ result })

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> =
        resultProvider().fold(
            onSuccess = { characters -> LoadResult.Page(data = characters, prevKey = null, nextKey = null) },
            onFailure = { error -> LoadResult.Error(error) }
        )

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? = null
}
