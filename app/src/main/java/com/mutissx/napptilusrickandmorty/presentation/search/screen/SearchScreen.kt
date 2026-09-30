package com.mutissx.napptilusrickandmorty.presentation.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.presentation.components.FilterChipsRow
import com.mutissx.napptilusrickandmorty.presentation.components.ScreenTitle
import com.mutissx.napptilusrickandmorty.presentation.search.components.CharacterResults
import com.mutissx.napptilusrickandmorty.presentation.search.components.OfflineBanner
import com.mutissx.napptilusrickandmorty.presentation.search.components.SearchField
import com.mutissx.napptilusrickandmorty.presentation.search.components.isShowingGrid
import com.mutissx.napptilusrickandmorty.presentation.search.components.rememberCollapsingHeaderState
import com.mutissx.napptilusrickandmorty.presentation.search.viewmodel.SearchViewModel
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    onCharacterClick: (Int) -> Unit,
    viewModel: SearchViewModel
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val items = viewModel.results.collectAsLazyPagingItems()

    val gridState = rememberLazyGridState()
    val header = rememberCollapsingHeaderState(gridState)
    val headerHeight = with(LocalDensity.current) { header.heightPx.toDp() }

    // Loading, empty and error states don't scroll, so the header must not stay hidden over them.
    val isShowingGrid = items.isShowingGrid
    LaunchedEffect(isShowingGrid) {
        if (!isShowingGrid) header.expand()
    }

    // Everything from the title to the grid scrolls as one: the header sits on top of the grid,
    // moves away with it and comes back on any upward scroll.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            // The hidden header is pushed above this box: keep it from drawing over the status bar.
            .clipToBounds()
            .nestedScroll(header.nestedScrollConnection)
    ) {
        CharacterResults(
            items = items,
            gridState = gridState,
            topPadding = headerHeight,
            onCharacterClick = onCharacterClick,
            errorMessage = viewModel::errorMessage
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = header.offsetPx.roundToInt()) }
                .onSizeChanged { header.onHeaderMeasured(it.height) }
                // Dragging the header scrolls the grid too (and so collapses the header): on a
                // short or landscape screen the header can take most of the height, and it must
                // not be a dead zone. The chips keep their own horizontal scrolling.
                .scrollable(
                    state = gridState,
                    orientation = Orientation.Vertical,
                    reverseDirection = ScrollableDefaults.reverseDirection(
                        layoutDirection = LocalLayoutDirection.current,
                        orientation = Orientation.Vertical,
                        reverseScrolling = false
                    )
                )
                // Opaque, so the cards scrolling underneath don't show through.
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column {
                ScreenTitle(title = stringResource(R.string.nav_characters_title))

                SearchField(
                    query = query,
                    onQueryChange = viewModel::onQueryChange,
                    onClearQuery = viewModel::onClearQuery,
                    enabled = isOnline
                )

                FilterChipsRow(
                    selectedStatus = status,
                    selectedGender = gender,
                    onStatusSelected = viewModel::onStatusSelected,
                    onGenderSelected = viewModel::onGenderSelected,
                    enabled = isOnline
                )

                if (!isOnline) OfflineBanner()
            }

            // A new filter keeps the previous results on screen until the first page of the new
            // search arrives; a thin bar under the header signals that fresh results are coming.
            if (isShowingGrid && items.loadState.refresh is LoadState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
