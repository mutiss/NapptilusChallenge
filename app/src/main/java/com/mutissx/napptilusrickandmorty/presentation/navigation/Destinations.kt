
package com.mutissx.napptilusrickandmorty.presentation.navigation

sealed class Destination(val route: String) {
    data object Characters : Destination("characters")
    data object Favorites : Destination("favorites")
    data object CharacterDetail : Destination("character/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Int): String = "character/$id"
    }
}

val bottomBarRoutes: Set<String> = setOf(Destination.Characters.route, Destination.Favorites.route)
