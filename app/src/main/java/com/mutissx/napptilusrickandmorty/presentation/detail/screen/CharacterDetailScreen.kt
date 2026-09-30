package com.mutissx.napptilusrickandmorty.presentation.detail.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.napptilusrickandmorty.presentation.components.ErrorView
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.components.aboveSharedCharacterImage
import com.mutissx.napptilusrickandmorty.presentation.detail.components.BackButton
import com.mutissx.napptilusrickandmorty.presentation.detail.components.DetailContent
import com.mutissx.napptilusrickandmorty.presentation.detail.components.FavoriteButton
import com.mutissx.napptilusrickandmorty.presentation.detail.state.CharacterDetailUiState
import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import kotlinx.coroutines.flow.collectLatest

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
                    // Otherwise the flying image covers it until the transition ends.
                    .aboveSharedCharacterImage()
            )
        }
    }
}
