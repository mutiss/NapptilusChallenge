package com.mutissx.napptilusrickandmorty.data.mapper

import com.mutissx.napptilusrickandmorty.data.remote.model.CharacterDto
import com.mutissx.napptilusrickandmorty.data.remote.model.LocationRefDto
import com.mutissx.napptilusrickandmorty.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.fake.aCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterMappersTest {

    private fun dto(
        status: String = "Alive",
        gender: String = "Male",
        type: String = "",
        origin: LocationRefDto? = LocationRefDto(name = "Earth (C-137)"),
        location: LocationRefDto? = LocationRefDto(name = "Citadel of Ricks"),
        episode: List<String> = emptyList()
    ) = CharacterDto(
        id = 1,
        name = "Rick Sanchez",
        status = status,
        species = "Human",
        type = type,
        gender = gender,
        origin = origin,
        location = location,
        image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
        episode = episode
    )

    @Test
    fun `given API status strings in any case, when mapped, then they become the matching CharacterStatus`() {
        assertEquals(CharacterStatus.ALIVE, dto(status = "Alive").toDomain().status)
        assertEquals(CharacterStatus.DEAD, dto(status = "dead").toDomain().status)
        assertEquals(CharacterStatus.UNKNOWN, dto(status = "unknown").toDomain().status)
    }

    @Test
    fun `given an unexpected status string, when mapped, then it falls back to UNKNOWN instead of crashing`() {
        assertEquals(CharacterStatus.UNKNOWN, dto(status = "Schrödinger").toDomain().status)
    }

    @Test
    fun `given API gender strings, when mapped, then they become the matching CharacterGender`() {
        assertEquals(CharacterGender.FEMALE, dto(gender = "Female").toDomain().gender)
        assertEquals(CharacterGender.MALE, dto(gender = "Male").toDomain().gender)
        assertEquals(CharacterGender.GENDERLESS, dto(gender = "Genderless").toDomain().gender)
        assertEquals(CharacterGender.UNKNOWN, dto(gender = "unknown").toDomain().gender)
    }

    @Test
    fun `given episode resource URLs, when mapped, then only their trailing numeric ids are kept`() {
        val character = dto(
            episode = listOf(
                "https://rickandmortyapi.com/api/episode/1",
                "https://rickandmortyapi.com/api/episode/28",
                "https://rickandmortyapi.com/api/episode/not-a-number"
            )
        ).toDomain()

        assertEquals(listOf(1, 28), character.episodeIds)
    }

    @Test
    fun `given a blank type, when mapped, then type is null`() {
        assertNull(dto(type = "").toDomain().type)
    }

    @Test
    fun `given the literal unknown or a blank place, when mapped, then origin and location are null`() {
        val character = dto(
            origin = LocationRefDto(name = "unknown"),
            location = LocationRefDto(name = "")
        ).toDomain()

        assertNull(character.origin)
        assertNull(character.location)
    }

    @Test
    fun `given a character, when it round-trips through the favorites entity, then it is unchanged`() {
        val character = aCharacter(
            status = CharacterStatus.DEAD,
            gender = CharacterGender.GENDERLESS,
            type = "Parasite",
            episodeIds = listOf(3, 15, 51)
        )

        assertEquals(character, character.toEntity(addedAt = 1L).toDomain())
    }

    @Test
    fun `given a character with no episodes, when it round-trips through the entity, then episodeIds stays empty`() {
        val character = aCharacter(episodeIds = emptyList())

        assertEquals(emptyList<Int>(), character.toEntity(addedAt = 1L).toDomain().episodeIds)
    }

    @Test
    fun `given filter enums, when converted to query params, then they use the lowercase API values`() {
        assertEquals("alive", CharacterStatus.ALIVE.toQueryParam())
        assertEquals("genderless", CharacterGender.GENDERLESS.toQueryParam())
    }
}
