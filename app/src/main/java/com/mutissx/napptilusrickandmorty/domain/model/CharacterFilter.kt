package com.mutissx.napptilusrickandmorty.domain.model

/**
 * Search criteria for the character list. A blank [name] and null filters mean
 * "every character", which is what the list shows by default.
 */
data class CharacterFilter(
    val name: String = "",
    val status: CharacterStatus? = null,
    val gender: CharacterGender? = null
)
