package com.mutissx.napptilusrickandmorty.feature.characters.data.mapper

import com.mutissx.napptilusrickandmorty.feature.characters.data.remote.model.EpisodeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpisodeMappersTest {

    @Test
    fun `given a standard episode code, when mapped, then the season number is extracted`() {
        val episode = EpisodeDto(id = 28, name = "The Ricklantis Mixup", airDate = "September 10, 2017", episode = "S03E07")
            .toDomain()

        assertEquals(3, episode.season)
        assertEquals("S03E07", episode.code)
    }

    @Test
    fun `given a malformed episode code, when mapped, then season is null`() {
        assertNull(EpisodeDto(id = 1, name = "Pilot", episode = "PILOT").toDomain().season)
    }

    @Test
    fun `given a blank air date, when mapped, then airDate is null`() {
        assertNull(EpisodeDto(id = 1, name = "Pilot", airDate = "", episode = "S01E01").toDomain().airDate)
    }
}
