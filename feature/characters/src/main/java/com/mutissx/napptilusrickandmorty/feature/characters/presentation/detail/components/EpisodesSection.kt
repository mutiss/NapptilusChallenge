package com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Episode
import com.mutissx.napptilusrickandmorty.core.ui.components.EmptyView
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.EpisodeRow
import com.mutissx.napptilusrickandmorty.core.ui.components.ErrorView
import com.mutissx.napptilusrickandmorty.core.ui.components.LoadingView
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.state.EpisodesState

internal fun LazyListScope.episodesSection(
    episodes: EpisodesState,
    onRetryEpisodes: () -> Unit
) {
    when (episodes) {
        EpisodesState.Loading -> item(key = "episodes-loading") {
            LoadingView(
                modifier = Modifier
                    .padding(24.dp)
                    .testTag(TestTags.DETAIL_EPISODES_LOADING)
            )
        }
        is EpisodesState.Error -> item(key = "episodes-error") {
            ErrorView(
                message = episodes.message.asString(),
                onRetry = onRetryEpisodes,
                modifier = Modifier.testTag(TestTags.DETAIL_EPISODES_ERROR)
            )
        }
        is EpisodesState.Loaded -> if (episodes.items.isEmpty()) {
            item(key = "episodes-empty") {
                EmptyView(
                    message = stringResource(R.string.detail_empty_episodes),
                    modifier = Modifier.testTag(TestTags.DETAIL_EMPTY_EPISODES)
                )
            }
        } else {
            episodes.items.groupBy(Episode::season).forEach { (season, seasonEpisodes) ->
                item(key = "season-$season") { SeasonHeader(season = season) }
                items(seasonEpisodes, key = { "episode-${it.id}" }) { episode ->
                    EpisodeRow(
                        episode = episode,
                        modifier = Modifier.testTag(TestTags.DETAIL_EPISODE_ROW)
                    )
                }
            }
        }
    }
}
