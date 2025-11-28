package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AccentPalette(val paletteName: String) {
    DEFAULT("Padrão"),
    VIVID("Vívida"),
    FOREST("Floresta"),
    OCEAN("Oceano"),
    SUNSET("Poente"),
    LAVENDER("Lavanda")
}

/**
 * Gera um ColorScheme claro com base na paleta selecionada.
 */
fun lightColorSchemeFor(palette: AccentPalette): ColorScheme {
    val roles = when (palette) {
        AccentPalette.DEFAULT -> PadraoLight
        AccentPalette.VIVID -> VividaLight
        AccentPalette.FOREST -> FlorestaLight
        AccentPalette.OCEAN -> OceanoLight
        AccentPalette.SUNSET -> SunsetLight
        AccentPalette.LAVENDER -> LavenderLight
    }

    return lightColorScheme(
        primary = roles.primary,
        onPrimary = roles.onPrimary,
        primaryContainer = roles.primaryContainer,
        onPrimaryContainer = roles.onPrimaryContainer,
        secondary = roles.secondary,
        onSecondary = roles.onSecondary,
        secondaryContainer = roles.secondaryContainer,
        onSecondaryContainer = roles.onSecondaryContainer,
        tertiary = roles.tertiary,
        onTertiary = roles.onTertiary,
        tertiaryContainer = roles.tertiaryContainer,
        onTertiaryContainer = roles.onTertiaryContainer,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        error = LightError,
        onError = LightOnError,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant
    )
}

/**
 * Gera um ColorScheme escuro com base na paleta selecionada.
 */
fun darkColorSchemeFor(palette: AccentPalette): ColorScheme {
    val roles = when (palette) {
        AccentPalette.DEFAULT -> PadraoDark
        AccentPalette.VIVID -> VividaDark
        AccentPalette.FOREST -> FlorestaDark
        AccentPalette.OCEAN -> OceanoDark
        AccentPalette.SUNSET -> SunsetDark
        AccentPalette.LAVENDER -> LavenderDark
    }

    return darkColorScheme(
        primary = roles.primary,
        onPrimary = roles.onPrimary,
        primaryContainer = roles.primaryContainer,
        onPrimaryContainer = roles.onPrimaryContainer,
        secondary = roles.secondary,
        onSecondary = roles.onSecondary,
        secondaryContainer = roles.secondaryContainer,
        onSecondaryContainer = roles.onSecondaryContainer,
        tertiary = roles.tertiary,
        onTertiary = roles.onTertiary,
        tertiaryContainer = roles.tertiaryContainer,
        onTertiaryContainer = roles.onTertiaryContainer,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        error = DarkError,
        onError = DarkOnError,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant
    )
}

// Data classes to hold color roles for better organization
private data class ColorRoles(
    val primary: Color, val onPrimary: Color, val primaryContainer: Color, val onPrimaryContainer: Color,
    val secondary: Color, val onSecondary: Color, val secondaryContainer: Color, val onSecondaryContainer: Color,
    val tertiary: Color, val onTertiary: Color, val tertiaryContainer: Color, val onTertiaryContainer: Color
)

private val PadraoLight = ColorRoles(
    primary = Color(0xFF00BFA6), onPrimary = Color.Black, primaryContainer = Color(0xFFA7FFEB), onPrimaryContainer = Color(0xFF00201A),
    secondary = TealLight, onSecondary = Color.Black, secondaryContainer = Color(0xFFB2EBF2), onSecondaryContainer = Color(0xFF001F22),
    tertiary = CyanDark, onTertiary = Color.White, tertiaryContainer = Color(0xFFB2EBF2), onTertiaryContainer = Color(0xFF001F22)
)
private val PadraoDark = ColorRoles(
    primary = Color(0xFF6FFFE6), onPrimary = Color(0xFF003731), primaryContainer = Color(0xFF005048), onPrimaryContainer = Color(0xFFA7FFEB),
    secondary = TealLight, onSecondary = Color.Black, secondaryContainer = Color(0xFF004E56), onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = CyanDark, onTertiary = Color.White, tertiaryContainer = Color(0xFF004E56), onTertiaryContainer = Color(0xFFB2EBF2)
)

private val VividaLight = ColorRoles(
    primary = Color(0xFFF4511E), onPrimary = Color.White, primaryContainer = Color(0xFFFFCCBC), onPrimaryContainer = Color(0xFF3E0E00),
    secondary = Color(0xFFFFB300), onSecondary = Color.Black, secondaryContainer = Color(0xFFFFECB3), onSecondaryContainer = Color(0xFF261900),
    tertiary = Color(0xFFE53935), onTertiary = Color.White, tertiaryContainer = Color(0xFFFFCDD2), onTertiaryContainer = Color(0xFF410002)
)
private val VividaDark = ColorRoles(
    primary = Color(0xFFFF8A65), onPrimary = Color(0xFF5F1D00), primaryContainer = Color(0xFFD84315), onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFD54F), onSecondary = Color(0xFF3F2E00), secondaryContainer = Color(0xFFFFB300), onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFEF9A9A), onTertiary = Color(0xFF610F0F), tertiaryContainer = Color(0xFFE53935), onTertiaryContainer = Color.White
)
private val FlorestaLight = ColorRoles(
    primary = Color(0xFF00796B), onPrimary = Color.White, primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF00201D),
    secondary = Color(0xFF8BC34A), onSecondary = Color.Black, secondaryContainer = Color(0xFFDCEDC8), onSecondaryContainer = Color(0xFF1B1D11),
    tertiary = Color(0xFF795548), onTertiary = Color.White, tertiaryContainer = Color(0xFFD7CCC8), onTertiaryContainer = Color(0xFF2E150F)
)
private val FlorestaDark = ColorRoles(
    primary = Color(0xFF4DB6AC), onPrimary = Color(0xFF003732), primaryContainer = Color(0xFF00796B), onPrimaryContainer = Color.White,
    secondary = Color(0xFFAED581), onSecondary = Color(0xFF2B341B), secondaryContainer = Color(0xFF8BC34A), onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFA1887F), onTertiary = Color(0xFF472922), tertiaryContainer = Color(0xFF795548), onTertiaryContainer = Color.White
)
private val OceanoLight = ColorRoles(
    primary = Color(0xFF0277BD), onPrimary = Color.White, primaryContainer = Color(0xFFB3E5FC), onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF00BCD4), onSecondary = Color.Black, secondaryContainer = Color(0xFFB2EBF2), onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFFFBC02D), onTertiary = Color.Black, tertiaryContainer = Color(0xFFFFF9C4), onTertiaryContainer = Color(0xFF251A00)
)
private val OceanoDark = ColorRoles(
    primary = Color(0xFF4FC3F7), onPrimary = Color(0xFF003349), primaryContainer = Color(0xFF0277BD), onPrimaryContainer = Color.White,
    secondary = Color(0xFF4DD0E1), onSecondary = Color(0xFF00363B), secondaryContainer = Color(0xFF00BCD4), onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFFFF176), onTertiary = Color(0xFF3A3000), tertiaryContainer = Color(0xFFFBC02D), onTertiaryContainer = Color.Black
)

// NOVAS PALETAS
private val SunsetLight = ColorRoles(
    primary = Color(0xFFEF6C00), onPrimary = Color.White, primaryContainer = Color(0xFFFFE0B2), onPrimaryContainer = Color(0xFF2F1500),
    secondary = Color(0xFFFB8C00), onSecondary = Color.Black, secondaryContainer = Color(0xFFFFECB3), onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Color(0xFFF57C00), onTertiary = Color.White, tertiaryContainer = Color(0xFFFFE0B2), onTertiaryContainer = Color(0xFF321600)
) // Corrigido: Removido ')' extra
private val SunsetDark = ColorRoles(
    primary = Color(0xFFFFB74D), onPrimary = Color(0xFF4D2600), primaryContainer = Color(0xFFEF6C00), onPrimaryContainer = Color.White,
    secondary = Color(0xFFFFB74D), onSecondary = Color(0xFF452700), secondaryContainer = Color(0xFFFB8C00), onSecondaryContainer = Color.Black,
    tertiary = Color(0xFFFFB74D), onTertiary = Color(0xFF4F2800), tertiaryContainer = Color(0xFFF57C00), onTertiaryContainer = Color.Black
) // Corrigido: Removido ')' extra
private val LavenderLight = ColorRoles(
    primary = Color(0xFF5E35B1), onPrimary = Color.White, primaryContainer = Color(0xFFD1C4E9), onPrimaryContainer = Color(0xFF1D0060),
    secondary = Color(0xFF7E57C2), onSecondary = Color.White, secondaryContainer = Color(0xFFE9DDFF), onSecondaryContainer = Color(0xFF28104E),
    tertiary = Color(0xFFB39DDB), onTertiary = Color.Black, tertiaryContainer = Color(0xFFF0EFFF), onTertiaryContainer = Color(0xFF27134A)
) // Corrigido: Removido ')' extra
private val LavenderDark = ColorRoles(
    primary = Color(0xFFB39DDB), onPrimary = Color(0xFF300090), primaryContainer = Color(0xFF5E35B1), onPrimaryContainer = Color.White,
    secondary = Color(0xFFB39DDB), onSecondary = Color(0xFF3F287A), secondaryContainer = Color(0xFF7E57C2), onSecondaryContainer = Color.White,
    tertiary = Color(0xFFD1C4E9), onTertiary = Color(0xFF3E2766), tertiaryContainer = Color(0xFFB39DDB), onTertiaryContainer = Color.Black
) // Corrigido: Removido ')' extra