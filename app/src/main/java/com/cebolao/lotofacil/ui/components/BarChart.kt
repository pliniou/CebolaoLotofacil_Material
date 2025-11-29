package com.cebolao.lotofacil.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

/**
 * Gráfico de Barras Moderno e Otimizado.
 * - Barras com cantos arredondados apenas no topo.
 * - Grid pontilhado sutil.
 * - Tooltip flutuante ao tocar.
 * - Animação suave na entrada.
 */
@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight
) {
    val density = LocalDensity.current
    val animProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Configuração de Cores e Pincéis (Memoized para performance no onDraw)
    val colors = ChartColors(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.tertiary, // Cor de destaque para barra selecionada
        text = MaterialTheme.colorScheme.onSurfaceVariant,
        line = MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppConfig.UI.CHART_GRID_ALPHA),
        tooltipBg = MaterialTheme.colorScheme.inverseSurface,
        tooltipText = MaterialTheme.colorScheme.inverseOnSurface
    )
    
    val paints = remember(density, colors) { ChartPaints(density, colors) }

    LaunchedEffect(data) {
        selectedIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(AppConfig.Animation.LONG_DURATION))
    }

    Box(modifier = modifier.height(chartHeight).padding(vertical = Dimen.SmallPadding)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val dimen = ChartDimensions(size.toSize(), Dimen.BarChartYAxisLabelWidth.toPx(), Dimen.BarChartXAxisLabelHeight.toPx())
                        val barWidth = dimen.calculateBarWidth(data.size, Dimen.MediumPadding.toPx())
                        
                        // Hit detection simples
                        val index = data.indices.firstOrNull { i ->
                            val left = dimen.yAxisWidth + i * (barWidth + Dimen.MediumPadding.toPx())
                            // Área de toque expandida horizontalmente para facilitar
                            val touchAreaLeft = left - (Dimen.MediumPadding.toPx() / 2)
                            val touchAreaRight = left + barWidth + (Dimen.MediumPadding.toPx() / 2)
                            offset.x in touchAreaLeft..touchAreaRight
                        }
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                }
        ) {
            val dimen = ChartDimensions(size, Dimen.BarChartYAxisLabelWidth.toPx(), Dimen.BarChartXAxisLabelHeight.toPx())
            val barWidth = dimen.calculateBarWidth(data.size, Dimen.MediumPadding.toPx())
            val barSpacing = Dimen.MediumPadding.toPx()

            // Desenhar Grid e Labels do Eixo Y
            drawGrid(maxValue, dimen, paints.text, colors.line)

            // Desenhar Barras
            data.forEachIndexed { index, (label, value) ->
                val targetBarHeight = (value.toFloat() / maxValue) * dimen.chartHeight
                val currentBarHeight = targetBarHeight * animProgress.value
                
                val left = dimen.yAxisWidth + index * (barWidth + barSpacing)
                val top = dimen.topPadding + dimen.chartHeight - currentBarHeight
                
                val isSelected = selectedIndex == index
                
                if (currentBarHeight > 0) {
                    // Gradiente sutil nas barras
                    val brush = Brush.verticalGradient(
                        colors = if (isSelected) 
                            listOf(colors.secondary, colors.secondary.copy(alpha = 0.8f))
                        else 
                            listOf(colors.primary, colors.primary.copy(alpha = 0.8f)),
                        startY = top,
                        endY = top + currentBarHeight
                    )

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, currentBarHeight),
                        cornerRadius = CornerRadius(Dimen.SmallPadding.toPx(), Dimen.SmallPadding.toPx()) // Arredondado
                    )
                }

                // Label do Eixo X (Rotacionado se necessário)
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(
                    AppConfig.UI.CHART_LABEL_ROTATION, 
                    left + barWidth / 2, 
                    size.height - dimen.xAxisHeight / 2
                )
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    left + barWidth / 2,
                    size.height - dimen.xAxisHeight / 4,
                    paints.label
                )
                drawContext.canvas.nativeCanvas.restore()
            }

            // Tooltip desenhado por último para ficar acima de tudo
            selectedIndex?.let { index ->
                val value = data[index].second
                val left = dimen.yAxisWidth + index * (barWidth + barSpacing)
                val targetBarHeight = (value.toFloat() / maxValue) * dimen.chartHeight
                val currentBarHeight = targetBarHeight * animProgress.value
                val top = dimen.topPadding + dimen.chartHeight - currentBarHeight
                
                drawTooltip(
                    centerX = left + barWidth / 2,
                    bottomY = top - Dimen.ExtraSmallPadding.toPx(),
                    text = value.toString(),
                    bg = colors.tooltipBg,
                    paint = paints.tooltip
                )
            }
        }
    }
}

// Helpers Isolados
private data class ChartColors(
    val primary: Color, val secondary: Color, val text: Color, val line: Color, val tooltipBg: Color, val tooltipText: Color
)

private class ChartPaints(density: androidx.compose.ui.unit.Density, colors: ChartColors) {
    val text = Paint().apply {
        isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.dp.toPx() }; textAlign = Paint.Align.RIGHT
    }
    val label = Paint().apply {
        isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.dp.toPx() }; textAlign = Paint.Align.CENTER
    }
    val tooltip = Paint().apply {
        isAntiAlias = true; color = colors.tooltipText.toArgb(); textSize = density.run { 12.dp.toPx() }; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
}

private data class ChartDimensions(val size: Size, val yAxisWidth: Float, val xAxisHeight: Float) {
    val topPadding = 24f // Espaço extra para tooltips no topo
    val chartHeight = size.height - xAxisHeight - topPadding
    
    fun calculateBarWidth(count: Int, spacing: Float): Float {
        val availableWidth = size.width - yAxisWidth - (spacing * (count - 1))
        return (availableWidth / count).coerceAtLeast(1f)
    }
}

private fun DrawScope.drawGrid(max: Int, dimen: ChartDimensions, paint: Paint, color: Color) {
    val lines = AppConfig.UI.CHART_GRID_LINES
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Grid pontilhado maior
    
    for (i in 0..lines) {
        val normalizedY = 1f - i.toFloat() / lines
        val y = dimen.topPadding + dimen.chartHeight * normalizedY
        val value = (max * i.toFloat() / lines).roundToInt()
        
        // Linha do Grid
        drawLine(
            color = color,
            start = Offset(dimen.yAxisWidth, y),
            end = Offset(size.width, y),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
        // Valor do Eixo Y
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            dimen.yAxisWidth - 12f,
            y + 10f, 
            paint
        )
    }
}

private fun DrawScope.drawTooltip(centerX: Float, bottomY: Float, text: String, bg: Color, paint: Paint) {
    val padding = 16f
    val textWidth = paint.measureText(text)
    val width = textWidth + padding * 2
    val height = 50f
    
    // Tooltip estilo "Balão"
    val path = Path().apply {
        // Retângulo arredondado
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = centerX - width / 2,
                top = bottomY - height,
                right = centerX + width / 2,
                bottom = bottomY,
                cornerRadius = CornerRadius(8f, 8f)
            )
        )
        // Triângulo indicador na parte inferior
        moveTo(centerX - 10f, bottomY)
        lineTo(centerX, bottomY + 10f)
        lineTo(centerX + 10f, bottomY)
        close()
    }

    drawPath(path, color = bg)
    drawContext.canvas.nativeCanvas.drawText(text, centerX, bottomY - height / 2 + 10f, paint)
}