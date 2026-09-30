package com.mutissx.napptilusrickandmorty.core.persistence

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Builds a feature's Room database with the app-wide defaults. The database class, its entities
 * and DAOs stay in the feature that owns them; this module only knows how to build one.
 */
inline fun <reified T : RoomDatabase> Context.buildRoomDatabase(
    name: String,
    noinline configure: RoomDatabase.Builder<T>.() -> Unit = {}
): T = Room.databaseBuilder(applicationContext, T::class.java, name)
    .apply(configure)
    .build()
