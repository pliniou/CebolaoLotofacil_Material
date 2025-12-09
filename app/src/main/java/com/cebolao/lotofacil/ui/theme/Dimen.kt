package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Spacing System (Golden Ratio / Fibonacci Approximations) ---
    // 4, 8, 12, 20, 32, 52, 84
    val SpacingXS = 4.dp
    val SpacingS = 8.dp
    val SpacingM = 12.dp 
    val SpacingL = 20.dp
    val SpacingXL = 32.dp
    val SpacingXXL = 52.dp
    val Spacing3XL = 84.dp

    // --- Aliases for Semantic Usage ---
    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS
    val MediumPadding = SpacingM
    val LargePadding = SpacingL
    val ExtraLargePadding = SpacingXL
    
    val SectionSpacing = SpacingXL
    val ItemSpacing = SpacingS

    // --- Layout ---
    val ScreenPadding = SpacingL // 20dp lateral padding for clean look
    val CardContentPadding = SpacingM
    val BottomContentPadding = 100.dp 
    val BottomBarSpacer = 84.dp 

    // --- Component Sizes ---
    val LargeButtonHeight = 52.dp // Golden ratioish
    val SmallButtonHeight = 32.dp 
    val MinButtonWidth = 128.dp 
    val PaletteCardWidth = 128.dp
    val PaletteCardHeight = 84.dp
    val HeroCardMinHeight = 180.dp 
    val ProgressBarHeight = 12.dp // Chunky & Flat
    
    // --- Icons ---
    val SmallIcon = 20.dp 
    val MediumIcon = 24.dp
    val LargeIcon = 32.dp
    val ExtraLargeIcon = 52.dp
    val ActionIconSize = 24.dp
    val Logo = 84.dp

    // --- Lotofácil Balls ---
    val BallSizeLarge = 52.dp
    val BallSizeMedium = 32.dp 
    val BallSizeSmall = 24.dp
    val BallSpacing = 4.dp
    
    val BallTextLarge = 20.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 10.sp

    // --- Charts ---
    val ChartHeight = 220.dp 
    val CheckResultChartHeight = 180.dp
    val BarChartHeight = 200.dp

    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Thick = 2.dp
    }
    
    object Elevation {
        val Low = 2.dp
        val Medium = 8.dp
        val High = 16.dp
    }
}