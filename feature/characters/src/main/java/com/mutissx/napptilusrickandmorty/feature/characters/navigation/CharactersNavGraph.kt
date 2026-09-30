package com.mutissx.napptilusrickandmorty.feature.characters.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mutissx.napptilusrickandmorty.core.ui.transition.ProvideNavAnimatedVisibilityScope
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.screen.CharacterDetailScreen
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.viewmodel.CharacterDetailViewModel
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.screen.FavoritesScreen
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.favorites.viewmodel.FavoritesViewModel
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.screen.SearchScreen
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.search.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * The feature's screens: the characters list, favourites and the character detail. The app hosts
 * them in its NavHost; [innerPadding] is the space its scaffold reserves (e.g. the bottom bar).
 */
fun NavGraphBuilder.charactersGraph(
    navController: NavController,
    innerPadding: PaddingValues
) {
    val onCharacterClick: (Int) -> Unit = { id ->
        navController.navigate(Destination.CharacterDetail.createRoute(id))
    }

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
