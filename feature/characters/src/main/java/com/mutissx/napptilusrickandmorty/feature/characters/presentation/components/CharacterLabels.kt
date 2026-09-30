package com.mutissx.napptilusrickandmorty.feature.characters.presentation.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus

// Presentation-only knowledge about domain enums (labels, colors) lives here, so the domain
// layer stays free of Android resources.

// Character status indicators: character knowledge, so they live here rather than in the
// feature-agnostic core theme.
private val StatusAlive = Color(0xFF55CC44)
private val StatusDead = Color(0xFFE5483B)
private val StatusUnknown = Color(0xFF9EA7B0)

@get:StringRes
val CharacterStatus.labelRes: Int
    get() = when (this) {
        CharacterStatus.ALIVE -> R.string.status_alive
        CharacterStatus.DEAD -> R.string.status_dead
        CharacterStatus.UNKNOWN -> R.string.status_unknown
    }

val CharacterStatus.color: Color
    get() = when (this) {
        CharacterStatus.ALIVE -> StatusAlive
        CharacterStatus.DEAD -> StatusDead
        CharacterStatus.UNKNOWN -> StatusUnknown
    }

@get:StringRes
val CharacterGender.labelRes: Int
    get() = when (this) {
        CharacterGender.FEMALE -> R.string.gender_female
        CharacterGender.MALE -> R.string.gender_male
        CharacterGender.GENDERLESS -> R.string.gender_genderless
        CharacterGender.UNKNOWN -> R.string.gender_unknown
    }
