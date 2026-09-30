package com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.feature.characters.R

@Composable
internal fun SeasonHeader(season: Int?) {
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
