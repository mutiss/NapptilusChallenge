package com.mutissx.napptilusrickandmorty.presentation.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.ui.theme.StatusAlive
import com.mutissx.napptilusrickandmorty.ui.theme.StatusDead
import com.mutissx.napptilusrickandmorty.ui.theme.StatusUnknown

// Presentation-only knowledge about domain enums (labels, colors) lives here, so the domain
// layer stays free of Android resources.

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
