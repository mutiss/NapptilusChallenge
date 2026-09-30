package com.mutissx.napptilusrickandmorty.core.ui.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Scopes needed by shared-element transitions between screens, provided by the nav host.
 *
 * They are exposed as nullable CompositionLocals instead of screen parameters so screens stay
 * usable on their own (previews, UI tests): without a provider every modifier here is a no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Call inside a nav destination so its content can take part in shared-element transitions. */
@Composable
fun AnimatedVisibilityScope.ProvideNavAnimatedVisibilityScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}

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
 * only by this z-index. Anything drawn on top of an image in the normal layout must sit in a
 * higher layer, or it's hidden for the whole transition and pops in at the end.
 */
enum class SharedLayer(val zIndex: Float) {
    IMAGE(0f),

    /** Gradients over artwork: above the image, below the text on it. */
    SCRIM(1f),

    /** Text, badges and controls drawn over the artwork. */
    CONTENT(2f)
}

/** Identical content on both screens: only one copy is drawn, flying between the two bounds. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElementOrNoOp(key: String, layer: SharedLayer): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedElementOrNoOp.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = SharedBoundsTransform,
            zIndexInOverlay = layer.zIndex
        )
    }
}

/**
 * Different content on each screen sharing the same bounds: both copies crossfade while the
 * bounds animate. Use [ResizeMode.scaleToBounds] for text that must not re-wrap mid-animation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsOrNoOp(
    key: String,
    layer: SharedLayer,
    // Same default as Compose's own sharedBounds.
    resizeMode: ResizeMode = scaleToBounds(ContentScale.FillWidth, Alignment.Center)
): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedBoundsOrNoOp.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(screenTransitionSpec()),
            exit = fadeOut(screenTransitionSpec()),
            boundsTransform = SharedBoundsTransform,
            resizeMode = resizeMode,
            zIndexInOverlay = layer.zIndex
        )
    }
}

/**
 * For content drawn over shared artwork that has no counterpart on the other screen (e.g. a back
 * button): it joins the overlay in [layer] and fades with its screen, instead of being covered by
 * the flying image until the transition ends.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.aboveSharedElements(layer: SharedLayer = SharedLayer.CONTENT): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        with(visibilityScope) {
            this@aboveSharedElements
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = layer.zIndex)
                // Overlay content isn't affected by the screen's own fade, so it fades itself.
                .animateEnterExit(
                    enter = fadeIn(screenTransitionSpec()),
                    exit = fadeOut(screenTransitionSpec())
                )
        }
    }
}
