package com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.CharacterCard
import com.mutissx.napptilusrickandmorty.core.ui.components.EmptyView
import com.mutissx.napptilusrickandmorty.core.ui.components.ErrorView
import com.mutissx.napptilusrickandmorty.core.ui.components.LoadingView
import com.mutissx.napptilusrickandmorty.core.ui.components.ScreenTitle
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.viewmodel.FavoritesViewModel

private val GRID_MIN_CELL_SIZE = 156.dp

@Composable
fun FavoritesScreen(
    innerPadding: PaddingValues,
    onCharacterClick: (Int) -> Unit,
    viewModel: FavoritesViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ScreenTitle(title = stringResource(R.string.nav_favorites_title))
        when (val currentState = state) {
            FavoritesUiState.Loading -> LoadingView(
                modifier = Modifier.testTag(TestTags.FAVORITES_LOADING_INDICATOR)
            )
            FavoritesUiState.Error -> ErrorView(
                message = stringResource(R.string.favorites_error_generic),
                modifier = Modifier.testTag(TestTags.FAVORITES_ERROR_VIEW)
            )
            is FavoritesUiState.Content -> if (currentState.favorites.isEmpty()) {
                EmptyView(
                    message = stringResource(R.string.favorites_empty),
                    icon = Icons.Outlined.FavoriteBorder,
                    modifier = Modifier.testTag(TestTags.FAVORITES_EMPTY_VIEW)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = GRID_MIN_CELL_SIZE),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.FAVORITES_GRID),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentState.favorites, key = { it.id }) { character ->
                        CharacterCard(
                            character = character,
                            onClick = { onCharacterClick(character.id) },
                            modifier = Modifier
                                .animateItem()
                                .testTag(TestTags.FAVORITES_CHARACTER_CARD)
                        )
                    }
                }
            }
        }
    }
}
