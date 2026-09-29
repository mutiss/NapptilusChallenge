package com.mutissx.napptilusrickandmorty.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCharacterDao {

    @Query("SELECT * FROM favorite_characters ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteCharacterEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_characters WHERE id = :id)")
    fun observeIsFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_characters WHERE id = :id")
    suspend fun getById(id: Int): FavoriteCharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteCharacterEntity)

    @Query("DELETE FROM favorite_characters WHERE id = :id")
    suspend fun delete(id: Int)
}
