package com.mutissx.napptilusrickandmorty.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.presentation.components.EmptyView
import com.mutissx.napptilusrickandmorty.presentation.components.ErrorView
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags

@Composable
internal fun CharacterResults(
    items: LazyPagingItems<Character>,
    onCharacterClick: (Int) -> Unit,
    errorMessage: (Throwable) -> UiText,
    modifier: Modifier = Modifier
) {
    val refresh = items.loadState.refresh
    Box(modifier = modifier.fillMaxSize()) {
        when (refresh) {
            is LoadState.Loading if items.itemCount == 0 -> LoadingView(
                modifier = Modifier.testTag(TestTags.INITIAL_LOADING_INDICATOR)
            )
            // A failed refresh means the current filter couldn't be loaded (typically offline and
            // never cached). Any items still on screen belong to the previous filter, so showing
            // them would make the chips look broken: replace them with the error instead.
            is LoadState.Error -> ErrorView(
                modifier = Modifier.testTag(TestTags.ERROR_VIEW),
                message = errorMessage(refresh.error).asString(),
                onRetry = { items.retry() }
            )

            is LoadState.NotLoading if items.itemCount == 0 -> EmptyView(
                modifier = Modifier.testTag(TestTags.EMPTY_VIEW_NO_RESULTS),
                message = stringResource(R.string.search_empty_no_results)
            )

            else -> {
                CharacterGrid(
                    items = items,
                    onCharacterClick = onCharacterClick,
                    errorMessage = errorMessage
                )
                // A new filter keeps the previous results on screen until the first page of
                // the new search arrives; a thin bar signals that fresh results are on the way.
                if (refresh is LoadState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}
