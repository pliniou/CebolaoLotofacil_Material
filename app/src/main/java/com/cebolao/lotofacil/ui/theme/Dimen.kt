package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimen {
    // --- Modern Spacing System (8pt Grid) ---
    val Spacing4 = 4.dp
    val Spacing8 = 8.dp
    val Spacing12 = 12.dp
    val Spacing16 = 16.dp   // Base unit
    val Spacing24 = 24.dp
    val Spacing32 = 32.dp
    val Spacing48 = 48.dp
    val Spacing64 = 64.dp
    val Spacing80 = 80.dp

    // --- Semantic Spacing (Short vs Long) ---
    val SpacingShort = Spacing8    // Between related items (e.g., text and icon)
    val SpacingMedium = Spacing16  // Standard padding
    val SpacingLong = Spacing32    // Between distinct sections

    // --- Aliases for Legacy Compatibility ---
    val SpacingXS = Spacing4
    val SpacingS = Spacing8
    val SpacingM = Spacing16 // Shifted to 16 for better breathing room
    val SpacingL = Spacing24
    val SpacingXL = Spacing32
    val SpacingXXL = Spacing48
    val Spacing3XL = Spacing80

    val ExtraSmallPadding = SpacingXS
    val SmallPadding = SpacingS
    val MediumPadding = SpacingM
    val LargePadding = SpacingL
    val ExtraLargePadding = SpacingXL
    
    val SectionSpacing = SpacingLong
    val ItemSpacing = SpacingShort

    // --- Layout ---
    val ScreenPadding = SpacingMedium // 16dp standard android margin
    val CardContentPadding = SpacingMedium
    val BottomContentPadding = 96.dp 
    val BottomBarSpacer = 80.dp 

    // --- Component Sizes ---
    val ActionButtonHeight = 48.dp // Standard minimum touch target
    val SmallButtonHeight = 32.dp 
    val MinButtonWidth = 120.dp
    
    val PaletteCardWidth = 120.dp
    val PaletteCardHeight = 80.dp
    val HeroCardMinHeight = 160.dp 
    val ProgressBarHeight = 8.dp 
    
    // --- Icons ---
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val IconExtraLarge = 48.dp

    // Legacy Aliases
    val SmallIcon = IconSmall
    val MediumIcon = IconMedium
    val LargeIcon = IconLarge
    val ExtraLargeIcon = IconExtraLarge
    val ActionIconSize = IconMedium
    val Logo = 80.dp
    
    // Compatibility
    val LargeButtonHeight = ActionButtonHeight
    val SpacingLarge = Spacing24

    // --- Lotofácil Balls ---
    val BallSizeLarge = 48.dp
    val BallSizeMedium = 32.dp 
    val BallSizeSmall = 24.dp
    val BallSpacing = 4.dp
    
    val BallTextLarge = 18.sp
    val BallTextMedium = 14.sp
    val BallTextSmall = 10.sp

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
        val None = 0.dp
        val Low = 2.dp
        val Medium = 4.dp
        val High = 8.dp
    }
}