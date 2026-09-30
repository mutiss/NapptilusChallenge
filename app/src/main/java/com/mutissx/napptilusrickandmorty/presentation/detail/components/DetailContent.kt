package com.mutissx.napptilusrickandmorty.presentation.detail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.detail.state.EpisodesState

// Room for the FAB so the last episode row is never hidden underneath it.
private val FAB_CLEARANCE = 96.dp

@Composable
internal fun DetailContent(
    character: Character,
    episodes: EpisodesState,
    onRetryEpisodes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    // Once the hero scrolls away, content would run under the transparent status bar and clash
    // with the system icons; fade in a scrim behind them.
    val isHeroScrolledAway by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isHeroScrolledAway) 1f else 0f,
        label = "statusBarScrim"
    )

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.DETAIL_CONTENT_LIST),
            contentPadding = PaddingValues(bottom = FAB_CLEARANCE)
        ) {
            item(key = "hero") { CharacterHero(character = character) }
            item(key = "info") { CharacterInfo(character = character) }
            item(key = "episodes-header") {
                SectionTitle(
                    text = stringResource(R.string.detail_episodes_section, character.episodeIds.size)
                )
            }
            episodesSection(episodes = episodes, onRetryEpisodes = onRetryEpisodes)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .graphicsLayer { alpha = scrimAlpha }
                .background(MaterialTheme.colorScheme.background)
        )
    }
}
