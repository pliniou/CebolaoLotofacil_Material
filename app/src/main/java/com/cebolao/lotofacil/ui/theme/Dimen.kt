package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centraliza todas as dimensões da UI, incluindo espaçamentos, tamanhos,
 * elevações e bordas para garantir consistência visual em todo o app.
 */
object Dimen {

    //region Espaçamentos (Padding)
    val ScreenPadding = 16.dp
    val LargePadding = 20.dp
    val CardPadding = 16.dp
    val MediumPadding = 8.dp
    val SmallPadding = 6.dp
    val ExtraSmallPadding = 4.dp
    val CardSpacing = 16.dp
    val SectionSpacing = 24.dp
    val BallSpacing = 6.dp
    val BottomBarOffset = 100.dp
    //endregion

    //region Tamanhos (Sizes)
    val LargeButtonHeight = 52.dp
    val NumberBall = 44.dp
    val NumberBallSmall = 38.dp
    val NumberBallDialog = 34.dp

    val SmallIcon = 18.dp
    val MediumIcon = 24.dp
    val LargeIcon = 28.dp

    val Logo = 64.dp
    val ProgressBarHeight = 6.dp
    val ProgressBarStroke = 1.5.dp
    val BarChartHeight = 200.dp
    val BarChartYAxisLabelWidth = 32.dp
    val BarChartXAxisLabelHeight = 36.dp
    val BarChartTooltipWidth = 56.dp
    val BarChartTooltipHeight = 28.dp

    val PaletteCardWidth = 120.dp
    val PaletteCardHeight = 100.dp
    //endregion

    //region Elevações (Elevation)
    object Elevation {
        val Level0 = 0.dp
        val Level1 = 1.dp
        val Level2 = 2.dp
        val Level3 = 4.dp
        val Level4 = 6.dp
    }
    //endregion

    //region Bordas (Border)
    object Border {
        val Default = 0.8.dp
        val Thick = 1.5.dp
    }
    //endregion

    //region Onboarding
    val ActiveIndicatorWidth = 20.dp
    val IndicatorHeight = 6.dp
    val IndicatorSpacing = 4.dp
    //endregion
}