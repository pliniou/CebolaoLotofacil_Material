package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dimensões refatoradas para um design mais "Airy" (arejado) e moderno.
 * Aumentamos o padding padrão e o raio das bordas.
 */
object Dimen {

    //region Espaçamentos (Padding & Margins)
    val ScreenPadding = 20.dp // Aumentado de 16dp para dar mais respiro nas bordas
    val LargePadding = 24.dp
    val CardPadding = 20.dp   // Cards com mais espaço interno
    val MediumPadding = 12.dp
    val SmallPadding = 8.dp
    val ExtraSmallPadding = 4.dp
    
    val SectionSpacing = 32.dp // Maior separação entre seções verticais
    val CardSpacing = 16.dp    // Espaço entre cards em uma lista
    val BallSpacing = 8.dp     // Espaço entre as bolas
    
    val BottomBarOffset = 120.dp // Espaço extra para FAB ou BottomBar flutuante
    //endregion

    //region Tamanhos (Sizes)
    val LargeButtonHeight = 56.dp // Botões mais altos (padrão moderno touch)
    
    // Bolas ligeiramente maiores para melhor toque e leitura
    val NumberBall = 46.dp
    val NumberBallSmall = 40.dp
    val NumberBallDialog = 36.dp

    val SmallIcon = 20.dp
    val MediumIcon = 24.dp
    val LargeIcon = 32.dp

    val Logo = 72.dp
    
    val ProgressBarHeight = 8.dp // Barra de progresso mais visível
    val ProgressBarStroke = 2.dp
    
    val BarChartHeight = 220.dp
    val BarChartYAxisLabelWidth = 36.dp
    val BarChartXAxisLabelHeight = 40.dp

    val PaletteCardWidth = 110.dp
    val PaletteCardHeight = 90.dp
    //endregion

    //region Elevações (Elevation)
    object Elevation {
        val None = 0.dp
        val Low = 2.dp
        val Medium = 4.dp
        val High = 8.dp
    }

    //region Bordas (Border)
    object Border {
        val Hairline = 0.5.dp
        val Thin = 1.dp
        val Regular = 1.5.dp
        val Thick = 2.dp
    }
    //endregion

    //region Onboarding
    val ActiveIndicatorWidth = 24.dp
    val IndicatorHeight = 8.dp
    val IndicatorSpacing = 6.dp
    //endregion
}