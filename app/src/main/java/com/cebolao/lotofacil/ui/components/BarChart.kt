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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.core.graphics.withSave
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

// --- Configuração ---
private const val Y_AXIS_WIDTH_PX = 70f
private const val X_AXIS_HEIGHT_PX = 70f
private const val TOP_PADDING_PX = 40f
private const val GRID_LINES_COUNT = 4
private const val TOOLTIP_POINTER_SIZE = 12f
private const val TOOLTIP_HEIGHT = 50f
private const val ROTATION_ANGLE = -45f

@Immutable
data class ChartColors(
    val primary: Color, val secondary: Color, val text: Color,
    val line: Color, val tooltipBg: Color, val tooltipText: Color
)

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight,
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

            drawChartGrid(maxValue, metrics, paints.text, colors.line)
            drawChartBars(data, maxValue, metrics, animProgress.value, selectedIndex, colors, paints)

            selectedIndex?.let { index ->
                drawChartTooltip(data[index].second, index, metrics, maxValue, animProgress.value, colors, paints)
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

// --- Helper Logic ---

private class ChartMetrics(size: Size, val dataCount: Int) {
    val drawHeight = size.height - X_AXIS_HEIGHT_PX - TOP_PADDING_PX
    val availableWidth = size.width - Y_AXIS_WIDTH_PX

    val barWidth: Float by lazy {
        val rawWidth = (availableWidth * 0.75f) / dataCount.coerceAtLeast(1)
        rawWidth.coerceIn(8f, 100f)
    }

    val barSpacing: Float by lazy {
        if (dataCount <= 1) 0f else {
            val totalBarWidth = dataCount * barWidth
            val availableSpace = availableWidth - totalBarWidth
            (availableSpace / (dataCount - 1)).coerceAtLeast(0f)
        }
    }

    fun getBarX(index: Int): Float = Y_AXIS_WIDTH_PX + index * (barWidth + barSpacing)

    fun getBarHeight(value: Int, max: Int): Float {
        return (value.toFloat() / max.coerceAtLeast(1)) * drawHeight
    }

    fun getBarIndexAt(x: Float): Int? {
        if (x < Y_AXIS_WIDTH_PX) return null
        val relativeX = x - Y_AXIS_WIDTH_PX
        val slotWidth = barWidth + barSpacing
        val index = (relativeX / slotWidth).toInt()

        if (index in 0 until dataCount) {
            val remainder = relativeX % slotWidth
            if (remainder <= barWidth + (barSpacing / 2)) return index
        }
        return null
    }
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

// --- Drawing Functions ---

private fun DrawScope.drawChartGrid(max: Int, metrics: ChartMetrics, paint: Paint, color: Color) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    for (i in 0..GRID_LINES_COUNT) {
        val normalizedY = 1f - i.toFloat() / GRID_LINES_COUNT
        val y = TOP_PADDING_PX + metrics.drawHeight * normalizedY
        val value = (max * i.toFloat() / GRID_LINES_COUNT).roundToInt()

        drawLine(
            color = color,
            start = Offset(Y_AXIS_WIDTH_PX, y),
            end = Offset(size.width, y),
            pathEffect = pathEffect,
            strokeWidth = 1f
        )
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            Y_AXIS_WIDTH_PX - 10f,
            y + 10f,
            paint
        )
    }
}

private fun DrawScope.drawChartBars(
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
        val top = TOP_PADDING_PX + metrics.drawHeight - currentHeight
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
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        // Desenha rótulo se não houver muitos dados
        if (data.size <= 15 || index % 2 == 0) {
            drawRotatedLabel(label, left + metrics.barWidth / 2, paints.label)
        }
    }
}

private fun DrawScope.drawRotatedLabel(text: String, x: Float, paint: Paint) {
    val y = size.height - X_AXIS_HEIGHT_PX / 3
    drawContext.canvas.nativeCanvas.withSave {
        rotate(ROTATION_ANGLE, x, y)
        drawText(text, x, y, paint)
    }
}

private fun DrawScope.drawChartTooltip(
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
    val top = TOP_PADDING_PX + metrics.drawHeight - currentHeight
    val centerX = left + metrics.barWidth / 2
    val bottomY = top - 12f

    val text = value.toString()
    val paddingHorizontal = 24f
    val textWidth = paints.tooltip.measureText(text)
    val width = textWidth + paddingHorizontal * 2

    val safeLeft = (centerX - width / 2).coerceIn(0f, size.width - width)
    val safeRight = safeLeft + width
    val safeTop = bottomY - TOOLTIP_HEIGHT

    val path = Path().apply {
        addRoundRect(RoundRect(
            left = safeLeft, top = safeTop, right = safeRight, bottom = bottomY,
            cornerRadius = CornerRadius(12f, 12f)
        ))
        val pointerX = centerX.coerceIn(safeLeft + 10f, safeRight - 10f)
        moveTo(pointerX - TOOLTIP_POINTER_SIZE / 2, bottomY)
        lineTo(pointerX, bottomY + TOOLTIP_POINTER_SIZE / 1.5f)
        lineTo(pointerX + TOOLTIP_POINTER_SIZE / 2, bottomY)
        close()
    }

    drawPath(path, color = colors.tooltipBg)
    drawContext.canvas.nativeCanvas.drawText(
        text,
        safeLeft + width / 2,
        safeTop + TOOLTIP_HEIGHT / 2 + 10f, // Centralização visual
        paints.tooltip
    )
}