package com.mutissx.napptilusrickandmorty.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("status") val status: String = "",
    @SerialName("species") val species: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("gender") val gender: String = "",
    @SerialName("origin") val origin: LocationRefDto? = null,
    @SerialName("location") val location: LocationRefDto? = null,
    @SerialName("image") val image: String = "",
    @SerialName("episode") val episode: List<String> = emptyList()
)

@Serializable
data class LocationRefDto(
    @SerialName("name") val name: String = "",
    @SerialName("url") val url: String = ""
)
