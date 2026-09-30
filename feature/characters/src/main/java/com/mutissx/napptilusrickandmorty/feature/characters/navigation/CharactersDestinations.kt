package com.mutissx.napptilusrickandmorty.feature.characters.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import com.mutissx.napptilusrickandmorty.core.ui.navigation.TopLevelDestination
import com.mutissx.napptilusrickandmorty.feature.characters.R

sealed class Destination(val route: String) {
    data object Characters : Destination("characters")
    data object Favorites : Destination("favorites")
    data object CharacterDetail : Destination("character/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Int): String = "character/$id"
    }
}

/** Where the app starts when this feature hosts the first screen. */
val charactersStartRoute: String = Destination.Characters.route

/** This feature's bottom-bar tabs, rendered by the app. */
val charactersTopLevelDestinations: List<TopLevelDestination> = listOf(
    TopLevelDestination(Destination.Characters.route, R.string.nav_characters_title, Icons.Filled.Groups),
    TopLevelDestination(Destination.Favorites.route, R.string.nav_favorites_title, Icons.Filled.Favorite)
)
