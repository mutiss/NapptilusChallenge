package com.mutissx.napptilusrickandmorty.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.mutissx.napptilusrickandmorty.core.ui.transition.LocalSharedTransitionScope
import com.mutissx.napptilusrickandmorty.core.ui.transition.screenTransitionSpec
import com.mutissx.napptilusrickandmorty.feature.characters.navigation.charactersGraph
import com.mutissx.napptilusrickandmorty.feature.characters.navigation.charactersStartRoute

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = charactersStartRoute,
                enterTransition = { if (isTabSwitch()) EnterTransition.None else fadeIn(screenTransitionSpec()) },
                exitTransition = { if (isTabSwitch()) ExitTransition.None else fadeOut(screenTransitionSpec()) },
                popEnterTransition = { fadeIn(screenTransitionSpec()) },
                popExitTransition = { fadeOut(screenTransitionSpec()) }
            ) {
                charactersGraph(navController = navController, innerPadding = innerPadding)
            }
        }
    }
}

// Switching bottom-bar tabs is instant: animating it would also make any shared element present
// in both tabs fly across the screen, which reads as a glitch.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.route in topLevelRoutes && targetState.destination.route in topLevelRoutes
