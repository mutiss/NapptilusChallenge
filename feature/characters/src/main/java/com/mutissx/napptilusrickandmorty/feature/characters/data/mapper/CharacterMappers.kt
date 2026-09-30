package com.mutissx.napptilusrickandmorty.feature.characters.data.mapper

import com.mutissx.napptilusrickandmorty.feature.characters.data.local.FavoriteCharacterEntity
import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.CharacterDto
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus

private const val UNKNOWN_VALUE = "unknown"
private const val EPISODE_IDS_SEPARATOR = ","

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status.toCharacterStatus(),
    species = species,
    type = type.takeIf { it.isNotBlank() },
    gender = gender.toCharacterGender(),
    origin = origin?.name.toKnownPlaceOrNull(),
    location = location?.name.toKnownPlaceOrNull(),
    imageUrl = image,
    episodeIds = episode.mapNotNull { it.toTrailingId() }
)

fun Character.toEntity(addedAt: Long): FavoriteCharacterEntity = FavoriteCharacterEntity(
    id = id,
    name = name,
    status = status.name,
    species = species,
    type = type,
    gender = gender.name,
    origin = origin,
    location = location,
    imageUrl = imageUrl,
    episodeIds = episodeIds.joinToString(EPISODE_IDS_SEPARATOR),
    addedAt = addedAt
)

fun FavoriteCharacterEntity.toDomain(): Character = Character(
    id = id,
    name = name,
    status = CharacterStatus.entries.find { it.name == status } ?: CharacterStatus.UNKNOWN,
    species = species,
    type = type,
    gender = CharacterGender.entries.find { it.name == gender } ?: CharacterGender.UNKNOWN,
    origin = origin,
    location = location,
    imageUrl = imageUrl,
    episodeIds = episodeIds.split(EPISODE_IDS_SEPARATOR).mapNotNull { it.toIntOrNull() }
)

fun CharacterStatus.toQueryParam(): String = name.lowercase()

fun CharacterGender.toQueryParam(): String = name.lowercase()

internal fun String.toCharacterStatus(): CharacterStatus = when (lowercase()) {
    "alive" -> CharacterStatus.ALIVE
    "dead" -> CharacterStatus.DEAD
    else -> CharacterStatus.UNKNOWN
}

internal fun String.toCharacterGender(): CharacterGender = when (lowercase()) {
    "female" -> CharacterGender.FEMALE
    "male" -> CharacterGender.MALE
    "genderless" -> CharacterGender.GENDERLESS
    else -> CharacterGender.UNKNOWN
}

// The API uses the literal "unknown" for missing places; treat it like a blank value so the UI
// can render a single localized fallback.
private fun String?.toKnownPlaceOrNull(): String? =
    this?.takeIf { it.isNotBlank() && !it.equals(UNKNOWN_VALUE, ignoreCase = true) }

// Resource links look like "https://rickandmortyapi.com/api/episode/28".
private fun String.toTrailingId(): Int? = substringAfterLast('/').toIntOrNull()
