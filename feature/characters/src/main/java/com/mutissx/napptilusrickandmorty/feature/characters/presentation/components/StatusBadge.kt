package com.mutissx.napptilusrickandmorty.feature.characters.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.core.ui.theme.NapptilusRickAndMortyTheme

private val BadgeBackground = Color.Black.copy(alpha = 0.6f)

/** Translucent pill meant to sit on top of character artwork. */
@Composable
fun StatusBadge(
    status: CharacterStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(BadgeBackground, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatusDot(status = status)
        Text(
            text = stringResource(status.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
fun StatusDot(
    status: CharacterStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(status.color, CircleShape)
    )
}

@Preview
@Composable
private fun StatusBadgePreview() {
    NapptilusRickAndMortyTheme {
        StatusBadge(status = CharacterStatus.ALIVE)
    }
}
