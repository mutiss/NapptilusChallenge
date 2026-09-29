package com.mutissx.napptilusrickandmorty.data.mapper

import com.mutissx.napptilusrickandmorty.data.remote.model.EpisodeDto
import com.mutissx.napptilusrickandmorty.domain.model.Episode

// Episode codes look like "S03E07".
private val EPISODE_CODE_REGEX = Regex("""S(\d+)E(\d+)""", RegexOption.IGNORE_CASE)

fun EpisodeDto.toDomain(): Episode = Episode(
    id = id,
    name = name,
    airDate = airDate?.takeIf { it.isNotBlank() },
    code = episode,
    season = EPISODE_CODE_REGEX.find(episode)?.groupValues?.get(1)?.toIntOrNull()
)
