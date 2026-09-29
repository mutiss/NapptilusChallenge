package com.mutissx.napptilusrickandmorty.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.ui.theme.NapptilusRickAndMortyTheme

@Composable
fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                CharacterImage(
                    character = character,
                    modifier = Modifier.fillMaxSize()
                )
                StatusBadge(
                    status = character.status,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = character.species,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Character artwork, tagged as a shared element so it flies between the grid and the detail
 * hero. The name is always rendered next to it, so the image itself is decorative.
 */
@Composable
fun CharacterImage(
    character: Character,
    modifier: Modifier = Modifier
) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(character.imageUrl)
            // Same key everywhere: the detail hero is served straight from the memory cache.
            .memoryCacheKey(character.imageUrl)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = placeholder,
        error = placeholder,
        modifier = modifier.sharedCharacterImage(character.id)
    )
}

@Preview(showBackground = true, widthDp = 180)
@Composable
private fun CharacterCardPreview() {
    NapptilusRickAndMortyTheme {
        CharacterCard(
            character = Character(
                id = 1,
                name = "Rick Sanchez",
                status = CharacterStatus.ALIVE,
                species = "Human",
                type = null,
                gender = CharacterGender.MALE,
                origin = "Earth (C-137)",
                location = "Citadel of Ricks",
                imageUrl = "",
                episodeIds = listOf(1, 2, 3)
            ),
            onClick = {}
        )
    }
}
