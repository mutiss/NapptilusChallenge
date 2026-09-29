package com.mutissx.napptilusrickandmorty.presentation.search.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.presentation.components.FilterChipsRow
import com.mutissx.napptilusrickandmorty.presentation.components.ScreenTitle
import com.mutissx.napptilusrickandmorty.presentation.search.components.CharacterResults
import com.mutissx.napptilusrickandmorty.presentation.search.components.OfflineBanner
import com.mutissx.napptilusrickandmorty.presentation.search.components.SearchField
import com.mutissx.napptilusrickandmorty.presentation.search.viewmodel.SearchViewModel

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
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

        CharacterResults(
            items = items,
            onCharacterClick = onCharacterClick,
            errorMessage = viewModel::errorMessage
        )
    }
}
