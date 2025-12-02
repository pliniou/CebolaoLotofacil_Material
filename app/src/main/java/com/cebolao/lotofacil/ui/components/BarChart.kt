package com.cebolao.lotofacil.ui.components

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.res.ResourcesCompat
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt
import androidx.core.graphics.withSave

// Padrões internos
private object ChartDefaults {
    val barCornerRadius = 4.dp
    val defaultChartHeight = 200.dp
}

@Immutable
data class ChartColors(
    val primary: Color, val secondary: Color, val text: Color,
    val line: Color, val tooltipBg: Color, val tooltipText: Color
)

private const val Y_AXIS_WIDTH = 70f
private const val X_AXIS_HEIGHT = 70f
private const val TOP_PADDING = 40f
private const val GRID_LINES = 4

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = ChartDefaults.defaultChartHeight,
    colors: ChartColors = defaultChartColors()
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val animProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val typeface = remember {
        ResourcesCompat.getFont(context, R.font.stacksansnotch_bold) ?: Typeface.DEFAULT_BOLD
    }

    val paints = remember(density, colors, typeface) {
        ChartPaints(density, colors, typeface)
    }

    LaunchedEffect(data) {
        selectedIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(AppConfig.Animation.LONG_DURATION))
    }

    Box(modifier = modifier.height(chartHeight).padding(vertical = Dimen.MediumPadding)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val metrics = ChartMetrics(size.toSize(), data.size)
                        val index = metrics.getBarIndexAt(offset.x)
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                }
        ) {
            val metrics = ChartMetrics(size, data.size)

            drawGrid(maxValue, metrics, paints.text, colors.line)
            drawBars(data, maxValue, metrics, animProgress.value, selectedIndex, colors, paints)

            selectedIndex?.let { index ->
                drawTooltip(data[index].second, index, metrics, maxValue, animProgress.value, colors, paints)
            }
        }
    }
}

@Composable
fun defaultChartColors(): ChartColors = ChartColors(
    primary = MaterialTheme.colorScheme.primary,
    secondary = MaterialTheme.colorScheme.tertiary,
    text = MaterialTheme.colorScheme.onSurfaceVariant,
    line = MaterialTheme.colorScheme.outlineVariant,
    tooltipBg = MaterialTheme.colorScheme.inverseSurface,
    tooltipText = MaterialTheme.colorScheme.inverseOnSurface
)

// --- Helpers de Desenho e Cálculo ---

private class ChartMetrics(val size: Size, val dataCount: Int) {
    val drawHeight = size.height - X_AXIS_HEIGHT - TOP_PADDING

    val barWidth: Float by lazy {
        val availableWidth = size.width - Y_AXIS_WIDTH
        val rawWidth = (availableWidth * 0.75f) / dataCount.coerceAtLeast(1)
        rawWidth.coerceIn(8f, 100f)
    }

    val barSpacing: Float by lazy {
        if (dataCount <= 1) 0f else {
            val totalBarWidth = dataCount * barWidth
            val availableSpace = (size.width - Y_AXIS_WIDTH) - totalBarWidth
            (availableSpace / (dataCount - 1)).coerceAtLeast(0f)
        }
    }

    fun getBarX(index: Int): Float = Y_AXIS_WIDTH + index * (barWidth + barSpacing)

    fun getBarHeight(value: Int, max: Int): Float {
        return (value.toFloat() / max.coerceAtLeast(1)) * drawHeight
    }

    fun getBarIndexAt(x: Float): Int? {
        if (x < Y_AXIS_WIDTH) return null
        val relativeX = x - Y_AXIS_WIDTH
        val slotWidth = barWidth + barSpacing
        val index = (relativeX / slotWidth).toInt()

        if (index in 0 until dataCount) {
            val remainder = relativeX % slotWidth
            if (remainder <= barWidth + (barSpacing / 2)) return index
        }
        return null
    }
}

private fun DrawScope.drawGrid(max: Int, metrics: ChartMetrics, paint: Paint, color: Color) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    for (i in 0..GRID_LINES) {
        val normalizedY = 1f - i.toFloat() / GRID_LINES
        val y = TOP_PADDING + metrics.drawHeight * normalizedY
        val value = (max * i.toFloat() / GRID_LINES).roundToInt()

        drawLine(
            color = color,
            start = Offset(Y_AXIS_WIDTH, y),
            end = Offset(size.width, y),
            pathEffect = pathEffect,
            strokeWidth = 1f
        )
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            Y_AXIS_WIDTH - 10f,
            y + 10f,
            paint
        )
    }
}

private fun DrawScope.drawBars(
    data: List<Pair<String, Int>>,
    max: Int,
    metrics: ChartMetrics,
    progress: Float,
    selectedIndex: Int?,
    colors: ChartColors,
    paints: ChartPaints
) {
    data.forEachIndexed { index, (label, value) ->
        val targetHeight = metrics.getBarHeight(value, max)
        val currentHeight = targetHeight * progress
        val left = metrics.getBarX(index)
        val top = TOP_PADDING + metrics.drawHeight - currentHeight
        val isSelected = selectedIndex == index

        if (currentHeight > 0) {
            val brush = Brush.verticalGradient(
                colors = if (isSelected)
                    listOf(colors.secondary, colors.secondary.copy(alpha = 0.8f))
                else
                    listOf(colors.primary, colors.primary.copy(alpha = 0.7f)),
                startY = top,
                endY = top + currentHeight
            )

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(metrics.barWidth, currentHeight),
                cornerRadius = CornerRadius(ChartDefaults.barCornerRadius.toPx(), ChartDefaults.barCornerRadius.toPx())
            )
        }

        if (data.size <= 15 || index % 2 == 0) {
            drawRotatedLabel(label, left + metrics.barWidth / 2, size.height - X_AXIS_HEIGHT / 4, paints.label)
        }
    }
}

private fun DrawScope.drawRotatedLabel(text: String, x: Float, y: Float, paint: Paint) {
    drawContext.canvas.nativeCanvas.withRotation(-45f, x, size.height - X_AXIS_HEIGHT / 3) {
        drawText(text, x, y, paint)
    }
}

private inline fun NativeCanvas.withRotation(degrees: Float, pivotX: Float, pivotY: Float, block: NativeCanvas.() -> Unit) {
    withSave {
        rotate(degrees, pivotX, pivotY)
        block()
    }
}

private fun DrawScope.drawTooltip(
    value: Int,
    index: Int,
    metrics: ChartMetrics,
    max: Int,
    progress: Float,
    colors: ChartColors,
    paints: ChartPaints
) {
    val left = metrics.getBarX(index)
    val targetHeight = metrics.getBarHeight(value, max)
    val currentHeight = targetHeight * progress
    val top = TOP_PADDING + metrics.drawHeight - currentHeight
    val centerX = left + metrics.barWidth / 2
    val bottomY = top - 12f

    val text = value.toString()
    val paddingHorizontal = 24f
    val textWidth = paints.tooltip.measureText(text)
    val width = textWidth + paddingHorizontal * 2
    val height = 50f

    val safeLeft = (centerX - width / 2).coerceIn(0f, size.width - width)
    val safeRight = safeLeft + width
    val safeTop = bottomY - height

    val path = Path().apply {
        addRoundRect(RoundRect(
            left = safeLeft, top = safeTop, right = safeRight, bottom = bottomY,
            cornerRadius = CornerRadius(12f, 12f)
        ))
        val pointerSize = 12f
        val pointerX = centerX.coerceIn(safeLeft + 10f, safeRight - 10f)
        moveTo(pointerX - pointerSize/2, bottomY)
        lineTo(pointerX, bottomY + pointerSize/1.5f)
        lineTo(pointerX + pointerSize/2, bottomY)
        close()
    }

    drawPath(path, color = colors.tooltipBg)
    drawContext.canvas.nativeCanvas.drawText(text, safeLeft + width/2, safeTop + height/2 + 10f, paints.tooltip)
}

private class ChartPaints(density: Density, colors: ChartColors, typeface: Typeface) {
    val text = Paint().apply {
        isAntiAlias = true
        color = colors.text.toArgb()
        textSize = density.run { 10.sp.toPx() }
        setTypeface(typeface)
        textAlign = Paint.Align.RIGHT
    }
    val label = Paint().apply {
        isAntiAlias = true
        color = colors.text.toArgb()
        textSize = density.run { 10.sp.toPx() }
        setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL))
        textAlign = Paint.Align.CENTER
    }
    val tooltip = Paint().apply {
        isAntiAlias = true
        color = colors.tooltipText.toArgb()
        textSize = density.run { 14.sp.toPx() }
        setTypeface(typeface)
        textAlign = Paint.Align.CENTER
    }
}