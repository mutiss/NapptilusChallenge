package com.mutissx.napptilusrickandmorty.fake

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeFavoritesRepository : FavoritesRepository {

    private val favorites = MutableStateFlow<List<Character>>(emptyList())

    val addedCharacters = mutableListOf<Character>()
    val removedIds = mutableListOf<Int>()

    var writeResult: Result<Unit, DataError.Local> = Result.Success(Unit)
    var readError: Throwable? = null

    override fun observeAll(): Flow<List<Character>> {
        val error = readError
        return if (error != null) flow { throw error } else favorites.asStateFlow()
    }

    override fun observeIsFavorite(id: Int): Flow<Boolean> =
        favorites.map { list -> list.any { it.id == id } }

    override suspend fun getFavorite(id: Int): Result<Character?, DataError.Local> =
        Result.Success(favorites.value.find { it.id == id })

    override suspend fun add(character: Character): Result<Unit, DataError.Local> {
        if (writeResult is Result.Success) {
            addedCharacters.add(character)
            favorites.value = favorites.value + character
        }
        return writeResult
    }

    override suspend fun remove(id: Int): Result<Unit, DataError.Local> {
        if (writeResult is Result.Success) {
            removedIds.add(id)
            favorites.value = favorites.value.filterNot { it.id == id }
        }
        return writeResult
    }
}
