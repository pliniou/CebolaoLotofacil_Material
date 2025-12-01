package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Grid System (Base 4dp/8dp) ---
    val ExtraSmallPadding = 2.dp
    val SmallPadding = 4.dp
    val MediumPadding = 8.dp
    val LargePadding = 12.dp
    val ExtraLargePadding = 16.dp
    val CardPadding = 16.dp

    // --- Semantic Aliases ---
    val ScreenPadding = ExtraLargePadding
    val CardContentPadding = ExtraLargePadding
    val SectionSpacing = 24.dp
    val ItemSpacing = LargePadding
    val ListSpacing = MediumPadding
    val BottomContentPadding = 80.dp

    // --- Component Tokens ---

    // Buttons
    val ButtonHeight = 48.dp
    val LargeButtonHeight = 56.dp
    val IconButtonSize = 48.dp // Touch target mínimo

    // Cards
    val CardCornerRadius = 16.dp
    val FilterCardCorner = 24.dp
    val PaletteCardWidth = 150.dp
    val PaletteCardHeight = 200.dp

    // Icons
    val SmallIcon = 18.dp
    val MediumIcon = 24.dp
    val LargeIcon = 32.dp
    val Logo = 120.dp

    // Progress Indicators
    val ProgressBarHeight = 8.dp
    val ProgressBarStroke = 4.dp
    val LoaderSize = 36.dp

    // Charts
    val BarChartHeight = 220.dp
    val ChartHeight = 220.dp
    val MiniChartHeight = 120.dp

    // Lotofácil Specifics (Balls)
    val BallSizeLarge = 48.dp
    val BallSizeMedium = 40.dp
    val BallSizeSmall = 32.dp
    val BallSpacing = MediumPadding

    val BallTextLarge = 20.sp
    val BallTextMedium = 16.sp
    val BallTextSmall = 12.sp

    // Widget Specifics
    val WidgetCornerRadius = 24.dp
    val WidgetContentPadding = 16.dp

    // Borders
    object Border {
        val Hairline = 0.5.dp
        val BorderThin = 1.dp
        val Regular = 2.dp
        val Thick = 3.dp
    }

    // Elevations
    object Elevation {
        val Low = 1.dp
        val Medium = 3.dp
        val High = 6.dp
    }
}
