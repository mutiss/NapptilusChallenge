package com.mutissx.napptilusrickandmorty.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Scopes needed by the list → detail shared-element transition, provided by the nav host.
 *
 * They are exposed as nullable CompositionLocals instead of screen parameters so screens stay
 * usable on their own (previews, UI tests): without a provider every modifier here is a no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * One duration and easing for everything that moves: the screens' fade, the shared bounds and
 * the fades of overlay content. If they drift apart, an element lands while its screen is still
 * fading (or vice versa) and visibly jumps in opacity.
 */
const val SCREEN_TRANSITION_DURATION_MS = 750

fun <T> screenTransitionSpec(): FiniteAnimationSpec<T> =
    tween(durationMillis = SCREEN_TRANSITION_DURATION_MS, easing = FastOutSlowInEasing)

@OptIn(ExperimentalSharedTransitionApi::class)
private val SharedBoundsTransform = BoundsTransform { _, _ -> screenTransitionSpec() }

/**
 * While a transition runs, shared elements are drawn in an overlay above both screens, ordered
 * only by this z-index. Anything drawn on top of the image in the normal layout must sit in a
 * higher layer, or it's hidden for the whole transition and pops in at the end.
 */
enum class SharedLayer(val zIndex: Float) {
    IMAGE(0f),

    /** The hero's gradient: above the artwork, below the text on it. */
    SCRIM(1f),

    /** Name, status badge and controls drawn over the artwork. */
    CONTENT(2f)
}

@Composable
fun Modifier.sharedCharacterImage(characterId: Int): Modifier =
    sharedCharacterElement(key = "character-image-$characterId", layer = SharedLayer.IMAGE)

/** The status badge is identical on both screens, so it simply flies to its new position. */
@Composable
fun Modifier.sharedCharacterStatus(characterId: Int): Modifier =
    sharedCharacterElement(key = "character-status-$characterId", layer = SharedLayer.CONTENT)

/**
 * The name uses a different text style on each screen, so instead of a shared element (which
 * shows only one of them) it morphs: both texts crossfade while being scaled between the card's
 * and the hero's bounds, without re-wrapping mid-animation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCharacterName(characterId: Int): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedCharacterName.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "character-name-$characterId"),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(screenTransitionSpec()),
            exit = fadeOut(screenTransitionSpec()),
            boundsTransform = SharedBoundsTransform,
            resizeMode = scaleToBounds(ContentScale.FillWidth, Alignment.CenterStart),
            zIndexInOverlay = SharedLayer.CONTENT.zIndex
        )
    }
}

/**
 * The hero's gradient, paired with an empty layer over the card's image. Sharing its bounds with
 * the image means it travels and resizes exactly like the artwork (so it never cuts a band across
 * it), while the crossfade with the empty card layer fades it in over the whole opening
 * transition and out over the whole return one.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCharacterScrim(characterId: Int): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedCharacterScrim.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "character-scrim-$characterId"),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(screenTransitionSpec()),
            exit = fadeOut(screenTransitionSpec()),
            boundsTransform = SharedBoundsTransform,
            zIndexInOverlay = SharedLayer.SCRIM.zIndex
        )
    }
}

/**
 * For content drawn over the artwork that has no counterpart on the other screen (the back
 * button): it joins the overlay in [layer] and fades with its screen, instead of being covered by
 * the flying image until the transition ends.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.aboveSharedCharacterImage(layer: SharedLayer = SharedLayer.CONTENT): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        with(visibilityScope) {
            this@aboveSharedCharacterImage
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = layer.zIndex)
                // Overlay content isn't affected by the screen's own fade, so it fades itself.
                .animateEnterExit(
                    enter = fadeIn(screenTransitionSpec()),
                    exit = fadeOut(screenTransitionSpec())
                )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.sharedCharacterElement(key: String, layer: SharedLayer): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedCharacterElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = SharedBoundsTransform,
            zIndexInOverlay = layer.zIndex
        )
    }
}
