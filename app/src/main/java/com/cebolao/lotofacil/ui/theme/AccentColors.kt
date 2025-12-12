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

/**
 * Gera o esquema de cores escuro baseado na paleta de acento,
 * mantendo um visual flat dark consistente em toda a aplicação.
 */
fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val primary = palette.seed

    // Secondary é quase sempre Rosa (ênfase em estatísticas).
    // Quando a primary já é Rosa, usamos Azul para preservar contraste.
    val secondary = when (palette) {
        AccentPalette.ROSA -> BrandAzul
        else -> BrandRosa
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = Color.Black,
        primaryContainer = primary.copy(alpha = 0.18f),
        onPrimaryContainer = primary,

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.18f),
        onSecondaryContainer = secondary,

        tertiary = BrandAmarelo,
        onTertiary = Color.Black,
        tertiaryContainer = BrandAmarelo.copy(alpha = 0.18f),
        onTertiaryContainer = BrandAmarelo,

        background = DarkBackground,
        onBackground = WhiteHighEmphasis,

        surface = DarkSurface,
        onSurface = WhiteHighEmphasis,
        surfaceVariant = DarkSurfaceElevated,
        onSurfaceVariant = WhiteMediumEmphasis,

        surfaceContainerLowest = DarkBackground,
        surfaceContainerLow = DarkBackground,
        surfaceContainer = DarkSurface,
        surfaceContainerHigh = DarkSurfaceElevated,
        surfaceContainerHighest = DarkSurfaceHighlight,

        outline = DarkSurfaceElevated,
        outlineVariant = DarkSurfaceElevated.copy(alpha = 0.5f),

        error = ErrorColor,
        onError = Color.White,
        errorContainer = ErrorColor.copy(alpha = 0.18f),
        onErrorContainer = ErrorColor,

        surfaceTint = primary,
        inverseSurface = WhiteHighEmphasis,
        inverseOnSurface = DarkBackground,
        inversePrimary = primary,
        scrim = Color.Black.copy(alpha = 0.32f)
    )
}
