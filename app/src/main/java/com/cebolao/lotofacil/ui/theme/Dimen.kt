package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Spacing System (4dp grid) ---
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp
    val SpacingL = 24.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 48.dp

    // --- Aliases for Semantic Usage ---
    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS
    val MediumPadding = SpacingM
    val LargePadding = SpacingL
    val ExtraLargePadding = SpacingXL
    
    val SectionSpacing = SpacingL
    val ItemSpacing = SpacingS

    // --- Layout ---
    val ScreenPadding = SpacingM
    val CardContentPadding = SpacingM
    val BottomContentPadding = 100.dp
    val BottomBarSpacer = 88.dp

    // --- Component Sizes ---
    val LargeButtonHeight = 56.dp
    val SmallButtonHeight = 40.dp
    val MinButtonWidth = 110.dp
    val PaletteCardWidth = 120.dp
    val PaletteCardHeight = 100.dp
    val HeroCardMinHeight = 180.dp
    val ProgressBarHeight = 8.dp
    
    // --- Icons ---
    val SmallIcon = 20.dp
    val MediumIcon = 24.dp
    val LargeIcon = 32.dp
    val ExtraLargeIcon = 64.dp
    val ActionIconSize = 24.dp
    val Logo = 96.dp

    // --- Lotofácil Balls ---
    val BallSizeLarge = 48.dp
    val BallSizeMedium = 32.dp // Better for 15-number grids
    val BallSizeSmall = 24.dp
    val BallSpacing = 4.dp
    
    val BallTextLarge = 18.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 10.sp

    // --- Charts ---
    val ChartHeight = 220.dp
    val CheckResultChartHeight = 160.dp
    val BarChartHeight = 200.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Thick = 2.dp
    }
}