package com.mutissx.napptilusrickandmorty.feature.characters.di

import com.mutissx.napptilusrickandmorty.core.network.interceptor.CachePolicies
import com.mutissx.napptilusrickandmorty.feature.characters.BuildConfig
import com.mutissx.napptilusrickandmorty.feature.characters.data.cache.CharacterMemoryCache
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.api.RickAndMortyApi
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.charactersCachePolicies
import com.mutissx.napptilusrickandmorty.feature.characters.data.repository.CharacterRepositoryImpl
import com.mutissx.napptilusrickandmorty.feature.characters.data.repository.FavoritesRepositoryImpl
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.CharacterRepository
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.FavoritesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val dataModule = module {
    // Collected by core:network's cache-control interceptor alongside other features' policies.
    single<CachePolicies>(named("characters")) { charactersCachePolicies }

    single<RickAndMortyApi> {
        get<Retrofit.Builder>()
            .baseUrl(BuildConfig.BASE_URL)
            .build()
            .create(RickAndMortyApi::class.java)
    }

    single { CharacterMemoryCache() }

    single<CharacterRepository> { CharacterRepositoryImpl(api = get(), memoryCache = get()) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(dao = get()) }
}
