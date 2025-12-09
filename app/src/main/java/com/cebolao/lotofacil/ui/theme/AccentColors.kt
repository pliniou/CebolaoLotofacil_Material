package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentPalette(val paletteName: String, val seed: Color) {
    AZUL("Azul Elétrico", BrandAzul),
    ROXO("Roxo Neon", BrandRoxo),
    VERDE("Verde Matrix", BrandVerde),
    AMARELO("Amarelo Laser", BrandAmarelo),
    ROSA("Rosa Choque", BrandRosa),
    LARANJA("Laranja Vivo", BrandLaranja)
}

fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = palette.seed
    // Design "Flat & Vivid": Manter a cor pura como Primária.
    
    val secondary = if (palette == AccentPalette.AMARELO) BrandRoxo else BrandAmarelo
    // Terciária para destaques extremos
    val tertiary = if (palette == AccentPalette.VERDE) BrandRosa else BrandVerde

    return darkColorScheme(
        primary = primary,
        onPrimary = if (palette == AccentPalette.AMARELO) Color.Black else Color.White,
        primaryContainer = primary.copy(alpha = 0.2f),
        onPrimaryContainer = primary, // Texto da container deve ser a propria cor vibrante para brilhar no dark
        
        secondary = secondary,
        onSecondary = if (secondary == BrandAmarelo) Color.Black else Color.White,
        secondaryContainer = secondary.copy(alpha = 0.2f),
        onSecondaryContainer = secondary,

        tertiary = tertiary,
        onTertiary = Color.Black,
        tertiaryContainer = tertiary.copy(alpha = 0.2f),
        onTertiaryContainer = tertiary,

        background = DarkBackground,
        onBackground = WhiteHighEmphasis,
        
        surface = DarkSurface,
        onSurface = WhiteHighEmphasis,
        
        surfaceVariant = DarkSurfaceElevated,
        onSurfaceVariant = WhiteMediumEmphasis,
        
        surfaceContainer = DarkSurfaceElevated,
        surfaceContainerHigh = DarkSurfaceHighlight,
        surfaceContainerLow = DarkBackground,
        
        outline = WhiteDisabled,
        outlineVariant = WhiteDisabled.copy(alpha = 0.3f),
        
        error = ErrorColor,
        onError = Color.White
    )
}