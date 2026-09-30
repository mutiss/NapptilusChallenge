package com.mutissx.napptilusrickandmorty.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.presentation.components.EmptyView
import com.mutissx.napptilusrickandmorty.presentation.components.ErrorView
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags

/**
 * Whether the results are shown as the grid, as opposed to a full-screen loading, error or empty
 * state. A failed refresh never shows the grid: any items still loaded belong to the previous
 * filter, so showing them would make the chips look broken.
 */
internal val LazyPagingItems<*>.isShowingGrid: Boolean
    get() = loadState.refresh !is LoadState.Error && itemCount > 0

/**
 * @param topPadding space reserved for the header drawn on top of the results.
 */
@Composable
internal fun CharacterResults(
    items: LazyPagingItems<Character>,
    gridState: LazyGridState,
    topPadding: Dp,
    onCharacterClick: (Int) -> Unit,
    errorMessage: (Throwable) -> UiText,
    modifier: Modifier = Modifier
) {
    if (items.isShowingGrid) {
        CharacterGrid(
            items = items,
            state = gridState,
            topPadding = topPadding,
            onCharacterClick = onCharacterClick,
            errorMessage = errorMessage,
            modifier = modifier
        )
        return
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = topPadding)
    ) {
        when (val refresh = items.loadState.refresh) {
            is LoadState.Error -> ErrorView(
                modifier = Modifier.testTag(TestTags.ERROR_VIEW),
                message = errorMessage(refresh.error).asString(),
                onRetry = { items.retry() }
            )
            is LoadState.Loading -> LoadingView(
                modifier = Modifier.testTag(TestTags.INITIAL_LOADING_INDICATOR)
            )
            is LoadState.NotLoading -> EmptyView(
                modifier = Modifier.testTag(TestTags.EMPTY_VIEW_NO_RESULTS),
                message = stringResource(R.string.search_empty_no_results)
            )
        }
    }
}
