package com.mutissx.napptilusrickandmorty.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A tab in the app's bottom bar. Features declare their own; the app only renders them, so it
 * never needs to know a feature's routes, labels or icons.
 */
data class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
)
