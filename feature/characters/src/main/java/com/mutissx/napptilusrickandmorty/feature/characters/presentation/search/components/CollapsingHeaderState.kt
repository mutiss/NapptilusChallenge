package com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * "Enter always" collapsing header: it scrolls away with the grid and slides back in as soon as
 * the user scrolls up, from anywhere in the list.
 *
 * The header is drawn on top of the grid and moved by [offsetPx]; the grid reserves the header's
 * height as top content padding. The connection never consumes scroll, so at the top of the list
 * header and grid move together, as if they were one scrolling column.
 */
@Stable
internal class CollapsingHeaderState(private val gridState: LazyGridState) {

    /** Measured height of the header; it changes when e.g. the offline banner appears. */
    var heightPx by mutableFloatStateOf(0f)
        private set

    /** How far the header is pushed up, from `-heightPx` (hidden) to `0` (fully shown). */
    var offsetPx by mutableFloatStateOf(0f)
        private set

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // A grid that fits on screen doesn't scroll: hiding the header would only open a gap.
            if (gridState.canScrollForward || gridState.canScrollBackward) {
                offsetPx = (offsetPx + available.y).coerceIn(-heightPx, 0f)
            }
            return Offset.Zero
        }
    }

    fun onHeaderMeasured(height: Int) {
        heightPx = height.toFloat()
        offsetPx = offsetPx.coerceIn(-heightPx, 0f)
    }

    fun expand() {
        offsetPx = 0f
    }
}

@Composable
internal fun rememberCollapsingHeaderState(gridState: LazyGridState): CollapsingHeaderState {
    val state = remember(gridState) { CollapsingHeaderState(gridState) }
    // Back at the very top the header must be fully shown, otherwise the grid's top padding would
    // leave an empty band under a half-hidden header (e.g. after the list jumps to a new search).
    LaunchedEffect(state) {
        snapshotFlow { gridState.canScrollBackward }.collect { canScrollUp ->
            if (!canScrollUp) state.expand()
        }
    }
    return state
}
