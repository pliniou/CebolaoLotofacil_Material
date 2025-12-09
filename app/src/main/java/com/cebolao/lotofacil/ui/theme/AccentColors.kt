package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentPalette(val paletteName: String, val seed: Color) {
    AZUL("Clássico", BrandAzul),
    ROXO("Lotofácil", BrandRoxo),
    VERDE("Sorte", BrandVerde),
    ROSA("Vibrante", BrandRosa),
    AMARELO("Luz", BrandAmarelo)
}

fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = palette.seed
    // Modern approach: Secondary is a complementary or variation, not just alpha
    val secondary = primary.copy(alpha = 0.9f)
    val tertiary = if (palette == AccentPalette.AZUL) BrandAmarelo.copy(alpha = 0.8f) else primary.copy(red = 1f - primary.red, alpha = 0.8f)

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.1f), // Lighter container
        onPrimaryContainer = primary.copy(alpha = 1f), // Strong text
        secondary = secondary,
        onSecondary = Color.White,
        tertiary = tertiary,
        background = LightBackground, 
        surface = LightSurface,
        surfaceContainer = LightSurfaceLow, // Mapped to new token
        surfaceContainerHigh = LightSurfaceHigh, // Mapped to new token
        onSurface = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutline.copy(alpha = 0.25f), // softer variant
        error = ErrorLight
    )
}

fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = palette.seed
    // In dark mode, primary should often be slightly lighter/desaturated for visibility, 
    // but for "Vibrant" look we keep it strong.
    val primaryLight = primary.copy(alpha = 1f) 

    return darkColorScheme(
        primary = primaryLight,
        onPrimary = Color.White, // Ensure contrast on dark primary
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = Color.White,
        secondary = primaryLight.copy(alpha = 0.8f),
        onSecondary = Color.Black,
        tertiary = if (palette == AccentPalette.AZUL) BrandAmarelo else primaryLight.copy(alpha = 0.8f),
        background = DarkBackground,
        surface = DarkSurface,
        // Using Low/High for containers in Dark mode
        surfaceContainer = DarkSurfaceLow,
        surfaceContainerHigh = DarkSurfaceHigh,
        onSurface = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutline.copy(alpha = 0.3f),
        error = ErrorDark
    )
}