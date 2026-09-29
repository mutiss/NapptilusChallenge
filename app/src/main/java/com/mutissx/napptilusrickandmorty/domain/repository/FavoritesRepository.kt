package com.mutissx.napptilusrickandmorty.domain.repository

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import com.mutissx.napptilusrickandmorty.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeAll(): Flow<List<Character>>
    fun observeIsFavorite(id: Int): Flow<Boolean>
    suspend fun getFavorite(id: Int): Result<Character?, DataError.Local>
    suspend fun add(character: Character): Result<Unit, DataError.Local>
    suspend fun remove(id: Int): Result<Unit, DataError.Local>
}
