package com.mutissx.napptilusrickandmorty.feature.characters.presentation.components

import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.mutissx.napptilusrickandmorty.core.ui.transition.SharedLayer
import com.mutissx.napptilusrickandmorty.core.ui.transition.aboveSharedElements
import com.mutissx.napptilusrickandmorty.core.ui.transition.sharedBoundsOrNoOp
import com.mutissx.napptilusrickandmorty.core.ui.transition.sharedElementOrNoOp

// The list → detail transition of a character, built on core's shared-transition plumbing:
// this file only knows which parts of a character move and how they stack.

@Composable
fun Modifier.sharedCharacterImage(characterId: Int): Modifier =
    sharedElementOrNoOp(key = "character-image-$characterId", layer = SharedLayer.IMAGE)

/** The status badge is identical on both screens, so it simply flies to its new position. */
@Composable
fun Modifier.sharedCharacterStatus(characterId: Int): Modifier =
    sharedElementOrNoOp(key = "character-status-$characterId", layer = SharedLayer.CONTENT)

/**
 * The name uses a different text style on each screen, so instead of a shared element (which
 * shows only one of them) it morphs: both texts crossfade while being scaled between the card's
 * and the hero's bounds, without re-wrapping mid-animation.
 */
@Composable
fun Modifier.sharedCharacterName(characterId: Int): Modifier =
    sharedBoundsOrNoOp(
        key = "character-name-$characterId",
        layer = SharedLayer.CONTENT,
        resizeMode = scaleToBounds(ContentScale.FillWidth, Alignment.CenterStart)
    )

/**
 * The hero's gradient, paired with an empty layer over the card's image. Sharing its bounds with
 * the image means it travels and resizes exactly like the artwork (so it never cuts a band across
 * it), while the crossfade with the empty card layer fades it in over the whole opening
 * transition and out over the whole return one.
 */
@Composable
fun Modifier.sharedCharacterScrim(characterId: Int): Modifier =
    sharedBoundsOrNoOp(key = "character-scrim-$characterId", layer = SharedLayer.SCRIM)

/** For controls over the hero (the back button) that have no counterpart on the list. */
@Composable
fun Modifier.aboveSharedCharacterImage(): Modifier = aboveSharedElements(SharedLayer.CONTENT)
