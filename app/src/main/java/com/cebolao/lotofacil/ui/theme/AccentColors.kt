package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentPalette(val paletteName: String, val primarySeed: Color) {
    DEFAULT("Padrão", BrandAzul), // Adicionado de volta para compatibilidade
    AZUL("Azul Clássico", BrandAzul),
    ROXO("Roxo Criativo", BrandRoxoEscuro),
    VERDE("Verde Sorte", BrandVerdeEscuro),
    ROSA("Rosa Vibrante", BrandRosaEscuro),
    AMARELO("Amarelo Luz", BrandAmarelo)
}

/**
 * Gera ColorScheme Light baseado na paleta.
 */
fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val (primary, secondary, tertiary, container) = when (palette) {
        AccentPalette.DEFAULT, AccentPalette.AZUL -> Quad(BrandAzul, BrandAmarelo, BrandAzulEscuro, BrandAzulClaro.copy(alpha=0.3f))
        AccentPalette.ROXO -> Quad(BrandRoxoEscuro, BrandVerdeClaro, BrandRoxoClaro, BrandRoxoClaro.copy(alpha=0.3f))
        AccentPalette.VERDE -> Quad(BrandVerdeEscuro, BrandAzulEscuro, BrandVerdeClaro, BrandVerdeClaro.copy(alpha=0.3f))
        AccentPalette.ROSA -> Quad(BrandRosaEscuro, BrandAmareloClaro, BrandRosaClaro, BrandRosaClaro.copy(alpha=0.3f))
        AccentPalette.AMARELO -> Quad(Color(0xFFC4C400), BrandAzul, BrandAmareloClaro, BrandAmareloClaro.copy(alpha=0.5f))
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = if(palette == AccentPalette.AMARELO) Color.Black else Color.White,
        primaryContainer = container,
        onPrimaryContainer = Color.Black,
        secondary = secondary,
        onSecondary = Color.Black,
        tertiary = tertiary,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant,
        error = LightError
    )
}

/**
 * Gera ColorScheme Dark baseado na paleta.
 */
fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val (primary, secondary, tertiary, container) = when (palette) {
        AccentPalette.DEFAULT, AccentPalette.AZUL -> Quad(BrandAzul, BrandAmarelo, BrandAzulClaro, BrandAzul.copy(alpha=0.2f))
        AccentPalette.ROXO -> Quad(BrandRoxoClaro, BrandVerdeClaro, BrandRoxoEscuro, BrandRoxoEscuro.copy(alpha=0.2f))
        AccentPalette.VERDE -> Quad(BrandVerdeClaro, BrandAzulClaro, BrandVerdeEscuro, BrandVerdeEscuro.copy(alpha=0.2f))
        AccentPalette.ROSA -> Quad(BrandRosaClaro, BrandAmarelo, BrandRosaEscuro, BrandRosaEscuro.copy(alpha=0.2f))
        AccentPalette.AMARELO -> Quad(BrandAmarelo, BrandAzulClaro, BrandAmareloClaro, BrandAmarelo.copy(alpha=0.2f))
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = if(palette == AccentPalette.AZUL || palette == AccentPalette.DEFAULT) Color.White else Color.Black,
        primaryContainer = container,
        onPrimaryContainer = Color.White,
        secondary = secondary,
        onSecondary = Color.Black,
        tertiary = tertiary,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        error = DarkError
    )
}

private data class Quad(val p: Color, val s: Color, val t: Color, val c: Color)