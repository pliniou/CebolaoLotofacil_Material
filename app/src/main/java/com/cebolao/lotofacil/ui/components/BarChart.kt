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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withSave
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList

private const val Y_AXIS_WIDTH_PX = 70f
private const val X_AXIS_HEIGHT_PX = 70f
private const val TOP_PADDING_PX = 40f
private const val GRID_LINES = 4

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    maxValue: Int,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val animProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val colors = ChartColors(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.outlineVariant,
        MaterialTheme.colorScheme.inverseSurface,
        MaterialTheme.colorScheme.inverseOnSurface
    )

    val typeface = remember { ResourcesCompat.getFont(context, R.font.stacksansnotch_bold) ?: Typeface.DEFAULT_BOLD }
    val paints = remember(density, colors) { ChartPaints(density, colors, typeface) }

    LaunchedEffect(data) {
        selectedIndex = null
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(AppConfig.Animation.LONG_DURATION))
    }

    Box(modifier = modifier.height(chartHeight).padding(vertical = Dimen.MediumPadding)) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(data) {
            detectTapGestures { offset ->
                selectedIndex = ChartMetrics(size.toSize(), data.size).getBarIndexAt(offset.x)
            }
        }) {
            val metrics = ChartMetrics(size, data.size)
            drawGrid(maxValue, metrics, paints.text, colors.line)
            drawBars(data, maxValue, metrics, animProgress.value, selectedIndex, colors, paints)
            selectedIndex?.let { idx -> drawTooltip(data[idx].second, idx, metrics, maxValue, animProgress.value, colors, paints) }
        }
    }
}

private data class ChartColors(
    val primary: Color, val secondary: Color, val text: Color,
    val line: Color, val tooltipBg: Color, val tooltipText: Color
)

private class ChartPaints(density: Density, colors: ChartColors, tf: Typeface) {
    val text = Paint().apply { isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.sp.toPx() }; typeface = tf; textAlign = Paint.Align.RIGHT }
    val label = Paint().apply { isAntiAlias = true; color = colors.text.toArgb(); textSize = density.run { 10.sp.toPx() }; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); textAlign = Paint.Align.CENTER }
    val tooltip = Paint().apply { isAntiAlias = true; color = colors.tooltipText.toArgb(); textSize = density.run { 14.sp.toPx() }; typeface = tf; textAlign = Paint.Align.CENTER }
}

private class ChartMetrics(size: Size, val count: Int) {
    val drawHeight = size.height - X_AXIS_HEIGHT_PX - TOP_PADDING_PX
    val availableWidth = size.width - Y_AXIS_WIDTH_PX
    val barWidth = ((availableWidth * 0.75f) / count.coerceAtLeast(1)).coerceIn(8f, 100f)
    val barSpacing = if (count <= 1) 0f else (availableWidth - (count * barWidth)) / (count - 1)

    fun getX(idx: Int) = Y_AXIS_WIDTH_PX + idx * (barWidth + barSpacing)
    fun getHeight(valVal: Int, max: Int) = (valVal.toFloat() / max.coerceAtLeast(1)) * drawHeight
    fun getBarIndexAt(x: Float): Int? {
        if (x < Y_AXIS_WIDTH_PX) return null
        val idx = ((x - Y_AXIS_WIDTH_PX) / (barWidth + barSpacing)).toInt()
        return if (idx in 0 until count && (x - Y_AXIS_WIDTH_PX) % (barWidth + barSpacing) <= barWidth) idx else null
    }
}

private fun DrawScope.drawGrid(max: Int, m: ChartMetrics, paint: Paint, color: Color) {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    for (i in 0..GRID_LINES) {
        val y = TOP_PADDING_PX + m.drawHeight * (1f - i.toFloat() / GRID_LINES)
        drawLine(color, Offset(Y_AXIS_WIDTH_PX, y), Offset(size.width, y), pathEffect = pathEffect)
        drawContext.canvas.nativeCanvas.drawText(((max * i) / GRID_LINES).toString(), Y_AXIS_WIDTH_PX - 10f, y + 10f, paint)
    }
}

private fun DrawScope.drawBars(data: List<Pair<String, Int>>, max: Int, m: ChartMetrics, prog: Float, selIdx: Int?, c: ChartColors, p: ChartPaints) {
    data.forEachIndexed { i, (lbl, v) ->
        val h = m.getHeight(v, max) * prog
        val x = m.getX(i)
        val y = TOP_PADDING_PX + m.drawHeight - h
        
        if (h > 0) {
            val color = if (selIdx == i) c.secondary else c.primary
            drawRoundRect(color = color, topLeft = Offset(x, y), size = Size(m.barWidth, h), cornerRadius = CornerRadius(8f, 8f))
        }
        if (data.size <= 15 || i % 2 == 0) {
            drawContext.canvas.nativeCanvas.withSave {
                rotate(-45f, x + m.barWidth / 2, size.height - 20f)
                drawText(lbl, x + m.barWidth / 2, size.height - 20f, p.label)
            }
        }
    }
}

private fun DrawScope.drawTooltip(v: Int, i: Int, m: ChartMetrics, max: Int, prog: Float, c: ChartColors, p: ChartPaints) {
    val x = m.getX(i) + m.barWidth / 2
    val y = TOP_PADDING_PX + m.drawHeight - (m.getHeight(v, max) * prog) - 12f
    val text = v.toString()
    val w = p.tooltip.measureText(text) + 48f
    val rect = RoundRect(x - w / 2, y - 50f, x + w / 2, y, CornerRadius(12f))
    drawPath(Path().apply { addRoundRect(rect); moveTo(x - 6f, y); lineTo(x, y + 8f); lineTo(x + 6f, y); close() }, c.tooltipBg)
    drawContext.canvas.nativeCanvas.drawText(text, x, y - 18f, p.tooltip)
}