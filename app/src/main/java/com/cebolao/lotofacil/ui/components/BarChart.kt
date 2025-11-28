package com.cebolao.lotofacil.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
    
    // Cores e Paints cacheados
    val colors = ChartColors(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.secondary,
        text = MaterialTheme.colorScheme.onSurfaceVariant,
        line = MaterialTheme.colorScheme.outlineVariant,
        tooltipBg = MaterialTheme.colorScheme.inverseSurface,
        tooltipText = MaterialTheme.colorScheme.inverseOnSurface
    )
    
    val paints = remember(density) { ChartPaints(density, colors) }

    LaunchedEffect(data) {
        selectedIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(AppConfig.Animation.LONG_DURATION))
    }

    Canvas(
        modifier = modifier
            .height(chartHeight)
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val dimen = ChartDimensions(size.toSize(), Dimen.BarChartYAxisLabelWidth.toPx(), Dimen.BarChartXAxisLabelHeight.toPx())
                    val barWidth = dimen.calculateBarWidth(data.size, Dimen.MediumPadding.toPx())
                    
                    val index = data.indices.firstOrNull { i ->
                        val left = dimen.yAxisWidth + i * (barWidth + Dimen.MediumPadding.toPx())
                        offset.x in left..(left + barWidth)
                    }
                    selectedIndex = if (selectedIndex == index) null else index
                }
            }
    ) {
        val dimen = ChartDimensions(size, Dimen.BarChartYAxisLabelWidth.toPx(), Dimen.BarChartXAxisLabelHeight.toPx())
        val barWidth = dimen.calculateBarWidth(data.size, Dimen.MediumPadding.toPx())
        val barSpacing = Dimen.MediumPadding.toPx()

        // Grid
        drawGrid(maxValue, dimen, paints.text, colors.line)

        // Barras
        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * dimen.chartHeight * animProgress.value
            val left = dimen.yAxisWidth + index * (barWidth + barSpacing)
            val top = dimen.topPadding + dimen.chartHeight - barHeight
            
            if (barHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(colors.primary, colors.secondary)),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(Dimen.SmallPadding.toPx())
                )
            }

            // Label Valor (acima da barra) - oculta se selecionado para não sobrepor tooltip
            if (selectedIndex != index) {
                drawContext.canvas.nativeCanvas.drawText(
                    value.toString(),
                    left + barWidth / 2,
                    top - Dimen.ExtraSmallPadding.toPx(),
                    paints.value
                )
            }

            // Label Eixo X (rotacionado)
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(
                AppConfig.UI.CHART_LABEL_ROTATION, 
                left + barWidth / 2, 
                size.height - dimen.xAxisHeight / 2
            )
            drawContext.canvas.nativeCanvas.drawText(
                label,
                left + barWidth / 2,
                size.height - dimen.xAxisHeight / 2,
                paints.label
            )
            drawContext.canvas.nativeCanvas.restore()
        }

        // Tooltip (desenhado por último)
        selectedIndex?.let { index ->
            val value = data[index].second
            val left = dimen.yAxisWidth + index * (barWidth + barSpacing)
            val barHeight = (value.toFloat() / maxValue) * dimen.chartHeight * animProgress.value
            val top = dimen.topPadding + dimen.chartHeight - barHeight
            
            drawTooltip(
                centerX = left + barWidth / 2,
                bottomY = top - Dimen.SmallPadding.toPx(),
                text = value.toString(),
                bg = colors.tooltipBg,
                paint = paints.tooltip
            )
        }
    }
}

// Classes auxiliares para limpar o Composable principal
private data class ChartColors(
    val primary: Color, val secondary: Color, val text: Color, val line: Color, val tooltipBg: Color, val tooltipText: Color
)

private class ChartPaints(density: androidx.compose.ui.unit.Density, colors: ChartColors) {
    val text = Paint().apply {
        isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.dp.toPx() }; textAlign = Paint.Align.RIGHT
    }
    val value = Paint().apply {
        isAntiAlias = true; color = colors.primary.toArgb(); textSize = density.run { 10.dp.toPx() }; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    val label = Paint().apply {
        isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.dp.toPx() }; textAlign = Paint.Align.CENTER
    }
    val tooltip = Paint().apply {
        isAntiAlias = true; color = colors.tooltipText.toArgb(); textSize = density.run { 12.dp.toPx() }; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
}

private data class ChartDimensions(val size: Size, val yAxisWidth: Float, val xAxisHeight: Float) {
    val topPadding = Dimen.CardPadding.value // Aproximação simplificada, idealmente converter DP
    val chartHeight = size.height - xAxisHeight - topPadding
    
    fun calculateBarWidth(count: Int, spacing: Float): Float {
        val availableWidth = size.width - yAxisWidth - (spacing * (count - 1))
        return (availableWidth / count).coerceAtLeast(1f)
    }
}

private fun DrawScope.drawGrid(max: Int, dimen: ChartDimensions, paint: Paint, color: Color) {
    val lines = AppConfig.UI.CHART_GRID_LINES
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
    
    for (i in 0..lines) {
        val y = dimen.topPadding + dimen.chartHeight * (1f - i.toFloat() / lines)
        val value = (max * i.toFloat() / lines).roundToInt()
        
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(dimen.yAxisWidth, y),
            end = Offset(size.width, y),
            pathEffect = pathEffect
        )
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            dimen.yAxisWidth - 8f,
            y + 4f, // Ajuste visual de baseline
            paint
        )
    }
}

private fun DrawScope.drawTooltip(centerX: Float, bottomY: Float, text: String, bg: Color, paint: Paint) {
    val width = 40f 
    val height = 24f
    drawRoundRect(
        color = bg,
        topLeft = Offset(centerX - width / 2, bottomY - height),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )
    drawContext.canvas.nativeCanvas.drawText(text, centerX, bottomY - 8f, paint)
}