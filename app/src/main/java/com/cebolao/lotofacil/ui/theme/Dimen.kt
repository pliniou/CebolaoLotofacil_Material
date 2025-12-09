package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Spacing System (4dp grid) - Expanded for Aero/Modern feel ---
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 16.dp // Restored to 16dp for better breathing
    val SpacingL = 24.dp // Restored to 24dp
    val SpacingXL = 32.dp
    val SpacingXXL = 48.dp
    val Spacing3XL = 64.dp

    // --- Aliases for Semantic Usage ---
    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS
    val MediumPadding = SpacingM
    val LargePadding = SpacingL
    val ExtraLargePadding = SpacingXL
    
    val SectionSpacing = SpacingL
    val ItemSpacing = SpacingS

    // --- Layout ---
    val ScreenPadding = SpacingL // More side padding
    val CardContentPadding = SpacingM
    val BottomContentPadding = 80.dp 
    val BottomBarSpacer = 72.dp 

    // --- Component Sizes ---
    val LargeButtonHeight = 56.dp // Taller, more tappable
    val SmallButtonHeight = 40.dp 
    val MinButtonWidth = 120.dp 
    val PaletteCardWidth = 120.dp
    val PaletteCardHeight = 96.dp
    val HeroCardMinHeight = 160.dp 
    val ProgressBarHeight = 8.dp // Slightly thicker
    
    // --- Icons ---
    val SmallIcon = 20.dp 
    val MediumIcon = 24.dp
    val LargeIcon = 32.dp
    val ExtraLargeIcon = 48.dp
    val ActionIconSize = 24.dp
    val Logo = 80.dp

    // --- Lotofácil Balls ---
    val BallSizeLarge = 44.dp
    val BallSizeMedium = 32.dp // Improved visibility
    val BallSizeSmall = 26.dp
    val BallSpacing = 4.dp // Better tap target separation
    
    val BallTextLarge = 18.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 11.sp

    // --- Charts ---
    val ChartHeight = 200.dp 
    val CheckResultChartHeight = 160.dp
    val BarChartHeight = 180.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Thick = 2.dp
    }
    
    object Elevation {
        val Low = 2.dp
        val Medium = 6.dp
        val High = 12.dp
    }
}