package com.mutissx.napptilusrickandmorty.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharactersResponseDto(
    @SerialName("info") val info: PageInfoDto = PageInfoDto(),
    @SerialName("results") val results: List<CharacterDto> = emptyList()
)

@Serializable
data class PageInfoDto(
    @SerialName("count") val count: Int = 0,
    @SerialName("pages") val pages: Int = 0,
    @SerialName("next") val next: String? = null,
    @SerialName("prev") val prev: String? = null
)
