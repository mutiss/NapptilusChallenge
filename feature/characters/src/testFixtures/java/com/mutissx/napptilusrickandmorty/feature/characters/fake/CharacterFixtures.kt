package com.mutissx.napptilusrickandmorty.feature.characters.fake

import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Episode

/** Builders with sensible defaults so each test only spells out what it cares about. */
fun aCharacter(
    id: Int = 1,
    name: String = "Rick Sanchez",
    status: CharacterStatus = CharacterStatus.ALIVE,
    species: String = "Human",
    type: String? = null,
    gender: CharacterGender = CharacterGender.MALE,
    origin: String? = "Earth (C-137)",
    location: String? = "Citadel of Ricks",
    imageUrl: String = "",
    episodeIds: List<Int> = listOf(1, 2)
) = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    origin = origin,
    location = location,
    imageUrl = imageUrl,
    episodeIds = episodeIds
)

fun anEpisode(
    id: Int = 1,
    name: String = "Pilot",
    airDate: String? = "December 2, 2013",
    code: String = "S01E01",
    season: Int? = 1
) = Episode(
    id = id,
    name = name,
    airDate = airDate,
    code = code,
    season = season
)
