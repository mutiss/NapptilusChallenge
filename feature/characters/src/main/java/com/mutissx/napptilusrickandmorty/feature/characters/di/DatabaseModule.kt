package com.mutissx.napptilusrickandmorty.feature.characters.di

import com.mutissx.napptilusrickandmorty.core.persistence.buildRoomDatabase
import com.mutissx.napptilusrickandmorty.feature.characters.data.local.CharactersDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single { androidApplication().buildRoomDatabase<CharactersDatabase>(CharactersDatabase.DB_NAME) }

    single { get<CharactersDatabase>().favoriteCharacterDao() }
}
