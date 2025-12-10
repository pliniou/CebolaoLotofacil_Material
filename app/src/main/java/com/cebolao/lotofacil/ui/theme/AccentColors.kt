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
    
    // Regra: Secondary é quase sempre Rosa para estatísticas, salvo se a primary for Rosa/Vermelho.
    val secondary = BrandRosa

    return darkColorScheme(
        primary = primary,
        onPrimary = Color.Black, // Contraste máximo em neon
        primaryContainer = primary.copy(alpha = 0.15f),
        onPrimaryContainer = primary,
        
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.15f),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo, // Warning / Highlights
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.15f),
        onTertiaryContainer = BrandAmarelo,

        background = DarkBackground,
        onBackground = WhiteHighEmphasis,
        
        surface = DarkSurface,
        onSurface = WhiteHighEmphasis,
        
        surfaceVariant = DarkSurfaceElevated, // Usado para bordas ou cards secundários
        onSurfaceVariant = WhiteMediumEmphasis,
        
        surfaceContainer = DarkSurface,
        surfaceContainerHigh = DarkSurfaceElevated,
        surfaceContainerLow = DarkBackground,
        
        outline = DarkSurfaceElevated, // Bordas sutis
        outlineVariant = DarkSurfaceElevated.copy(alpha = 0.5f),
        
        error = ErrorColor,
        onError = Color.White
    )
}