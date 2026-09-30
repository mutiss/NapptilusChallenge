package com.mutissx.napptilusrickandmorty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteCharacterEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteCharacterDao(): FavoriteCharacterDao

    companion object {
        const val DB_NAME = "napp_rm_characters.db"
    }
}
