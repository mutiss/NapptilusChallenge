package com.mutissx.napptilusrickandmorty.di

import com.mutissx.napptilusrickandmorty.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.data.repository.CharacterRepositoryImpl
import com.mutissx.napptilusrickandmorty.data.repository.FavoritesRepositoryImpl
import com.mutissx.napptilusrickandmorty.domain.repository.CharacterRepository
import com.mutissx.napptilusrickandmorty.domain.repository.FavoritesRepository
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    single<RickAndMortyApi> { get<Retrofit>().create(RickAndMortyApi::class.java) }

    single { CharacterMemoryCache() }

    single<CharacterRepository> { CharacterRepositoryImpl(api = get(), memoryCache = get()) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get()) }
}
