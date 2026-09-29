package com.mutissx.napptilusrickandmorty.domain.model

data class Character(
    val id: Int,
    val name: String,
    val status: CharacterStatus,
    val species: String,
    val type: String?,
    val gender: CharacterGender,
    val origin: String?,
    val location: String?,
    val imageUrl: String,
    val episodeIds: List<Int>
)

enum class CharacterStatus { ALIVE, DEAD, UNKNOWN }

enum class CharacterGender { FEMALE, MALE, GENDERLESS, UNKNOWN }
