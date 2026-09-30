package com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.components.labelRes

@Composable
internal fun CharacterInfo(
    character: Character,
    modifier: Modifier = Modifier
) {
    val unknown = stringResource(R.string.detail_unknown_value)
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoTile(
                icon = Icons.Outlined.Science,
                label = stringResource(R.string.detail_species),
                value = character.species.ifBlank { unknown },
                modifier = Modifier.weight(1f)
            )
            InfoTile(
                icon = Icons.Outlined.Face,
                label = stringResource(R.string.detail_gender),
                value = stringResource(character.gender.labelRes),
                modifier = Modifier.weight(1f)
            )
        }
        InfoTile(
            icon = Icons.Outlined.Public,
            label = stringResource(R.string.detail_origin),
            value = character.origin ?: unknown
        )
        InfoTile(
            icon = Icons.Outlined.Place,
            label = stringResource(R.string.detail_location),
            value = character.location ?: unknown
        )
        character.type?.let { type ->
            InfoTile(
                icon = Icons.Outlined.Category,
                label = stringResource(R.string.detail_type),
                value = type
            )
        }
    }
}
