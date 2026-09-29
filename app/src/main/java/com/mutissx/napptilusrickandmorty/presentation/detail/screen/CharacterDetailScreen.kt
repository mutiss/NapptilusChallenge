package com.mutissx.napptilusrickandmorty.presentation.detail.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.Episode
import com.mutissx.napptilusrickandmorty.presentation.components.CharacterImage
import com.mutissx.napptilusrickandmorty.presentation.components.EmptyView
import com.mutissx.napptilusrickandmorty.presentation.components.EpisodeRow
import com.mutissx.napptilusrickandmorty.presentation.components.ErrorView
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.StatusBadge
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.components.labelRes
import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import kotlinx.coroutines.flow.collectLatest

// Room for the FAB so the last episode row is never hidden underneath it.
private val FAB_CLEARANCE = 96.dp

@Composable
fun CharacterDetailScreen(
    onBack: () -> Unit,
    viewModel: CharacterDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.favoriteMessages.collectLatest { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // The hero image is drawn edge-to-edge behind the status bar; insets are handled inline.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        floatingActionButton = {
            (uiState as? CharacterDetailUiState.Content)?.let { content ->
                FavoriteButton(
                    isFavorite = content.isFavorite,
                    onClick = viewModel::onFavoriteToggle,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is CharacterDetailUiState.Loading -> LoadingView(
                    modifier = Modifier.testTag(TestTags.DETAIL_LOADING_INDICATOR)
                )
                is CharacterDetailUiState.Error -> ErrorView(
                    message = state.message.asString(),
                    onRetry = viewModel::loadInfo,
                    modifier = Modifier
                        .statusBarsPadding()
                        .testTag(TestTags.DETAIL_ERROR_VIEW)
                )
                is CharacterDetailUiState.Content -> DetailContent(
                    character = state.character,
                    episodes = state.episodes,
                    onRetryEpisodes = viewModel::retryEpisodes,
                    modifier = Modifier.fillMaxSize()
                )
            }

            BackButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Translucent disc so the arrow stays visible over any avatar.
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag(TestTags.DETAIL_BACK_BUTTON),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back_button_description)
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.testTag(TestTags.DETAIL_FAVORITE_BUTTON),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(0.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.remove_from_favorites_description
                else R.string.add_to_favorites_description
            )
        )
    }
}

@Composable
private fun DetailContent(
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

@Composable
private fun CharacterHero(
    character: Character,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background
    val portraitDescription = stringResource(R.string.character_image_description, character.name)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        CharacterImage(
            character = character,
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = portraitDescription }
        )
        // Fades the artwork into the page so the name reads on top of any image.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to background
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(status = character.status)
            Text(
                text = character.name,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )
        }
    }
}

@Composable
private fun CharacterInfo(
    character: Character,
    modifier: Modifier = Modifier
) {
    val unknown = stringResource(R.string.detail_unknown_value)
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoTile(
                icon = Icons.Outlined.Science,
                label = stringResource(R.string.detail_species),
                value = character.species.ifBlank { unknown },
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = Icons.Outlined.Face,
                label = stringResource(R.string.detail_gender),
                value = stringResource(character.gender.labelRes),
                modifier = Modifier.weight(1f)
            )
        }
        InfoTile(
            icon = Icons.Outlined.Public,
            label = stringResource(R.string.detail_origin),
            value = character.origin ?: unknown
        )
        InfoTile(
            icon = Icons.Outlined.Place,
            label = stringResource(R.string.detail_location),
            value = character.location ?: unknown
        )
        character.type?.let { type ->
            InfoTile(
                icon = Icons.Outlined.Category,
                label = stringResource(R.string.detail_type),
                value = type
            )
        }
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .semantics { heading() }
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun SeasonHeader(season: Int?) {
    Text(
        text = if (season != null) {
            stringResource(R.string.detail_season_header, season)
        } else {
            stringResource(R.string.detail_other_episodes_header)
        },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
    )
}

private fun LazyListScope.episodesSection(
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
