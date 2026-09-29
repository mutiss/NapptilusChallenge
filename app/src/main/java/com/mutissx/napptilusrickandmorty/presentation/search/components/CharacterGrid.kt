package com.mutissx.napptilusrickandmorty.presentation.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.presentation.components.CharacterCard
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags

private val GRID_MIN_CELL_SIZE = 156.dp
private const val CHARACTER_CONTENT_TYPE = "character"

@Composable
internal fun CharacterGrid(
    items: LazyPagingItems<Character>,
    onCharacterClick: (Int) -> Unit,
    errorMessage: (Throwable) -> UiText
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = GRID_MIN_CELL_SIZE),
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.CHARACTER_GRID),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id },
            contentType = items.itemContentType { CHARACTER_CONTENT_TYPE }
        ) { index ->
            val character = items[index] ?: return@items
            CharacterCard(
                character = character,
                onClick = { onCharacterClick(character.id) },
                modifier = Modifier.testTag(TestTags.CHARACTER_CARD)
            )
        }
        when (val append = items.loadState.append) {
            is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                LoadingView(
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag(TestTags.APPEND_LOADING_INDICATOR)
                )
            }
            is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                AppendErrorFooter(
                    message = errorMessage(append.error).asString(),
                    onRetry = { items.retry() }
                )
            }
            is LoadState.NotLoading -> Unit
        }
    }
}
