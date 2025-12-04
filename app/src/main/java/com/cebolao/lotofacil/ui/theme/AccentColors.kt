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
    AMARELO("Luz", Color(0xFF686000))
}

fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = if (palette == AccentPalette.AMARELO) Color(0xFF686000) else palette.seed
    val secondary = primary.copy(alpha = 0.8f)
    val tertiary = if (palette == AccentPalette.AZUL) BrandAmarelo.copy(alpha = 0.6f) else primary.copy(red = 1f - primary.red)

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.12f),
        onPrimaryContainer = primary.copy(alpha = 1f),
        secondary = secondary,
        onSecondary = Color.White,
        tertiary = tertiary,
        background = LightBackground,
        surface = LightSurface,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        onSurface = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutline.copy(alpha = 0.4f),
        error = ErrorLight
    )
}

fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = if (palette == AccentPalette.AMARELO) BrandAmarelo else palette.seed
    val primaryLight = primary.copy(alpha = 0.9f)

    return darkColorScheme(
        primary = primaryLight,
        onPrimary = if (palette == AccentPalette.AMARELO) Color.Black else Color.White,
        primaryContainer = primary.copy(alpha = 0.3f),
        onPrimaryContainer = Color.White,
        secondary = primaryLight.copy(alpha = 0.7f),
        onSecondary = Color.Black,
        tertiary = if (palette == AccentPalette.AZUL) BrandAmarelo else primaryLight,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        onSurface = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutline.copy(alpha = 0.4f),
        error = ErrorDark
    )
}