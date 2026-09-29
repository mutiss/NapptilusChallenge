package com.mutissx.napptilusrickandmorty.presentation.search.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.presentation.components.CharacterCard
import com.mutissx.napptilusrickandmorty.presentation.components.EmptyView
import com.mutissx.napptilusrickandmorty.presentation.components.ErrorView
import com.mutissx.napptilusrickandmorty.presentation.components.LoadingView
import com.mutissx.napptilusrickandmorty.presentation.components.RetryButton
import com.mutissx.napptilusrickandmorty.presentation.components.ScreenTitle
import com.mutissx.napptilusrickandmorty.presentation.components.TestTags
import com.mutissx.napptilusrickandmorty.presentation.search.viewmodel.SearchViewModel

private val GRID_MIN_CELL_SIZE = 156.dp
private const val CHARACTER_CONTENT_TYPE = "character"

@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    onCharacterClick: (Int) -> Unit,
    viewModel: SearchViewModel
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val items = viewModel.results.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        ScreenTitle(title = stringResource(R.string.nav_characters_title))

        SearchField(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onClearQuery = viewModel::onClearQuery
        )

        FilterChipsRow(
            selectedStatus = status,
            selectedGender = gender,
            onStatusSelected = viewModel::onStatusSelected,
            onGenderSelected = viewModel::onGenderSelected
        )

        CharacterResults(
            items = items,
            onCharacterClick = onCharacterClick,
            errorMessage = viewModel::errorMessage
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.testTag(TestTags.SEARCH_CLEAR_BUTTON)
                ) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.clear_search_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .testTag(TestTags.SEARCH_TEXT_FIELD),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // Results already update while typing; the IME action just dismisses the keyboard.
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
private fun CharacterResults(
    items: LazyPagingItems<Character>,
    onCharacterClick: (Int) -> Unit,
    errorMessage: (Throwable) -> UiText,
    modifier: Modifier = Modifier
) {
    val refresh = items.loadState.refresh
    Box(modifier = modifier.fillMaxSize()) {
        when {
            refresh is LoadState.Loading && items.itemCount == 0 -> LoadingView(
                modifier = Modifier.testTag(TestTags.INITIAL_LOADING_INDICATOR)
            )
            refresh is LoadState.Error && items.itemCount == 0 -> ErrorView(
                modifier = Modifier.testTag(TestTags.ERROR_VIEW),
                message = errorMessage(refresh.error).asString(),
                onRetry = { items.retry() }
            )
            refresh is LoadState.NotLoading && items.itemCount == 0 -> EmptyView(
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

@Composable
private fun CharacterGrid(
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

/** Inline, non-blocking error for a failed next page: what's already loaded stays usable. */
@Composable
private fun AppendErrorFooter(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(TestTags.APPEND_ERROR_VIEW),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.search_append_error),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        RetryButton(onRetry = onRetry)
    }
}
