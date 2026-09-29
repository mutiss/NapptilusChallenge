package com.mutissx.napptilusrickandmorty.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RickAndMortyColorScheme = darkColorScheme(
    primary = PortalGreen,
    onPrimary = OnPortalGreen,
    primaryContainer = SpaceSurfaceHigh,
    onPrimaryContainer = PortalGreen,
    secondary = PortalGreen,
    onSecondary = OnPortalGreen,
    secondaryContainer = SpaceSurfaceHigh,
    onSecondaryContainer = PortalGreen,
    tertiary = PortalCyan,
    onTertiary = OnPortalCyan,
    background = SpaceBackground,
    onBackground = TextPrimary,
    surface = SpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = SpaceSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SpaceSurface,
    surfaceContainerHigh = SpaceSurfaceHigh,
    outline = SpaceOutline,
    outlineVariant = SpaceOutline,
    error = ErrorRed,
    onError = OnErrorRed
)

@Composable
fun NapptilusRickAndMortyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RickAndMortyColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
