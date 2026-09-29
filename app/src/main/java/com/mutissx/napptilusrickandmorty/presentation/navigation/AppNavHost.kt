package com.mutissx.napptilusrickandmorty.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mutissx.napptilusrickandmorty.presentation.components.LocalNavAnimatedVisibilityScope
import com.mutissx.napptilusrickandmorty.presentation.components.LocalSharedTransitionScope
import com.mutissx.napptilusrickandmorty.presentation.detail.screen.CharacterDetailScreen
import com.mutissx.napptilusrickandmorty.presentation.detail.viewmodel.CharacterDetailViewModel
import com.mutissx.napptilusrickandmorty.presentation.favorites.screen.FavoritesScreen
import com.mutissx.napptilusrickandmorty.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.napptilusrickandmorty.presentation.search.screen.SearchScreen
import com.mutissx.napptilusrickandmorty.presentation.search.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

private const val TRANSITION_DURATION_MS = 350

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    val onCharacterClick: (Int) -> Unit = { id ->
        navController.navigate(Destination.CharacterDetail.createRoute(id))
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Destination.Characters.route,
                enterTransition = { if (isTabSwitch()) EnterTransition.None else fadeIn(tween(TRANSITION_DURATION_MS)) },
                exitTransition = { if (isTabSwitch()) ExitTransition.None else fadeOut(tween(TRANSITION_DURATION_MS)) },
                popEnterTransition = { fadeIn(tween(TRANSITION_DURATION_MS)) },
                popExitTransition = { fadeOut(tween(TRANSITION_DURATION_MS)) }
            ) {
                composable(Destination.Characters.route) {
                    ProvideNavAnimatedVisibilityScope {
                        val viewModel: SearchViewModel = koinViewModel()
                        SearchScreen(
                            innerPadding = innerPadding,
                            onCharacterClick = onCharacterClick,
                            viewModel = viewModel
                        )
                    }
                }
                composable(Destination.Favorites.route) {
                    ProvideNavAnimatedVisibilityScope {
                        val viewModel: FavoritesViewModel = koinViewModel()
                        FavoritesScreen(
                            innerPadding = innerPadding,
                            onCharacterClick = onCharacterClick,
                            viewModel = viewModel
                        )
                    }
                }
                composable(
                    route = Destination.CharacterDetail.route,
                    arguments = listOf(navArgument(Destination.CharacterDetail.ARG_ID) {
                        type = NavType.IntType
                    })
                ) {
                    ProvideNavAnimatedVisibilityScope {
                        val viewModel: CharacterDetailViewModel = koinViewModel()
                        CharacterDetailScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.ProvideNavAnimatedVisibilityScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
}

// Switching bottom-bar tabs is instant: animating it would also make any character present in
// both grids fly across the screen as a shared element, which reads as a glitch.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.route in bottomBarRoutes && targetState.destination.route in bottomBarRoutes
