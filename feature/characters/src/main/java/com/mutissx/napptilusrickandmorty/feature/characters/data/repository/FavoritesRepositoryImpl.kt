package com.mutissx.napptilusrickandmorty.feature.characters.data.repository

import com.mutissx.napptilusrickandmorty.core.persistence.safeDbCall
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.feature.characters.data.local.FavoriteCharacterDao
import com.mutissx.napptilusrickandmorty.feature.characters.data.mapper.toDomain
import com.mutissx.napptilusrickandmorty.feature.characters.data.mapper.toEntity
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteCharacterDao,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : FavoritesRepository {

    override fun observeAll(): Flow<List<Character>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeIsFavorite(id: Int): Flow<Boolean> =
        dao.observeIsFavorite(id)

    override suspend fun getFavorite(id: Int): Result<Character?, DataError.Local> =
        safeDbCall { dao.getById(id)?.toDomain() }

    override suspend fun add(character: Character): Result<Unit, DataError.Local> =
        safeDbCall { dao.insert(character.toEntity(addedAt = clock())) }

    override suspend fun remove(id: Int): Result<Unit, DataError.Local> =
        safeDbCall { dao.delete(id) }
}
