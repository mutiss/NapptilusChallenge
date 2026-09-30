package com.mutissx.napptilusrickandmorty.feature.characters.domain.model

data class Episode(
    val id: Int,
    val name: String,
    val airDate: String?,
    val code: String,
    val season: Int?
)
