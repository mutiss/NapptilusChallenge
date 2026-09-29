package com.mutissx.napptilusrickandmorty.data.remote.api

import com.mutissx.napptilusrickandmorty.data.remote.model.CharacterDto
import com.mutissx.napptilusrickandmorty.data.remote.model.CharactersResponseDto
import com.mutissx.napptilusrickandmorty.data.remote.model.EpisodeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {

    /** Null query params are dropped by Retrofit, so an all-null call lists every character. */
    @GET("character/")
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("gender") gender: String? = null
    ): CharactersResponseDto

    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): CharacterDto

    /**
     * Multiple-episode lookup in a single request. The bracket syntax (`episode/[1,2]`) makes
     * the API always answer with an array, even for a single id — the plain `episode/1` form
     * returns a bare object instead, which would break deserialization.
     */
    @GET("episode/[{ids}]")
    suspend fun getEpisodes(@Path("ids") ids: String): List<EpisodeDto>
}
