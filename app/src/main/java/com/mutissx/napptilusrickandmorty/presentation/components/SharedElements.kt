package com.mutissx.napptilusrickandmorty.presentation.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * Scopes needed by the list → detail shared-element transition, provided by the nav host.
 *
 * They are exposed as nullable CompositionLocals instead of screen parameters so screens stay
 * usable on their own (previews, UI tests): without a provider the modifier is a no-op.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCharacterImage(characterId: Int): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedCharacterImage.sharedElement(
            rememberSharedContentState(key = "character-image-$characterId"),
            visibilityScope
        )
    }
}
