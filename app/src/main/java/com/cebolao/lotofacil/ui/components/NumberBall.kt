package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallVariant { Primary, Secondary, Lotofacil }

@Immutable
private data class BallStyle(
    val backgroundBrush: Brush,
    val contentColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
    val elevation: Dp
)

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = Dimen.NumberBall,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Primary
) {
    val style = getBallStyle(isSelected, isHighlighted, isDisabled, variant)
    
    // Animações
    val elevation by animateDpAsState(style.elevation, label = "elevation")
    val contentColor by animateColorAsState(style.contentColor, tween(AppConfig.Animation.SHORT_DURATION), label = "content")
    val borderColor by animateColorAsState(style.borderColor, tween(AppConfig.Animation.SHORT_DURATION), label = "border")

    val desc = stringResource(
        R.string.number_ball_content_description,
        number,
        getStateDescription(isSelected, isHighlighted, isDisabled)
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, CircleShape)
            .clip(CircleShape)
            .background(style.backgroundBrush)
            .border(width = style.borderWidth, color = borderColor, shape = CircleShape)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DEFAULT_NUMBER_FORMAT.format(number),
            color = contentColor,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (size.value / 2.4).sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
            )
        )
    }
}

@Composable
private fun getBallStyle(
    isSelected: Boolean,
    isHighlighted: Boolean,
    isDisabled: Boolean,
    variant: NumberBallVariant
): BallStyle {
    val theme = MaterialTheme.colorScheme

    val baseColor = when (variant) {
        NumberBallVariant.Primary -> theme.primary
        NumberBallVariant.Secondary -> theme.secondary
        NumberBallVariant.Lotofacil -> theme.tertiary
    }
    
    val onBaseColor = when (variant) {
        NumberBallVariant.Primary -> theme.onPrimary
        NumberBallVariant.Secondary -> theme.onSecondary
        NumberBallVariant.Lotofacil -> theme.onTertiary
    }

    return when {
        isDisabled -> BallStyle(
            backgroundBrush = Brush.verticalGradient(listOf(theme.surfaceVariant, theme.surfaceVariant)),
            contentColor = theme.onSurfaceVariant.copy(alpha = 0.38f),
            borderColor = Color.Transparent,
            borderWidth = 0.dp,
            elevation = 0.dp
        )
        isSelected -> BallStyle(
            // Gradiente sutil de cima para baixo
            backgroundBrush = Brush.verticalGradient(
                listOf(baseColor.copy(alpha = 0.85f), baseColor)
            ),
            contentColor = onBaseColor,
            borderColor = Color.Transparent,
            borderWidth = 0.dp,
            elevation = Dimen.Elevation.Medium
        )
        isHighlighted -> BallStyle(
            backgroundBrush = Brush.verticalGradient(listOf(theme.surface, theme.surface)),
            contentColor = baseColor,
            borderColor = baseColor,
            borderWidth = 2.dp,
            elevation = Dimen.Elevation.Low
        )
        else -> BallStyle(
            // Estilo padrão clean
            backgroundBrush = Brush.verticalGradient(listOf(theme.surface, theme.surfaceContainerLow)),
            contentColor = theme.onSurface,
            borderColor = theme.outlineVariant.copy(alpha = 0.5f),
            borderWidth = 1.dp,
            elevation = Dimen.Elevation.Low
        )
    }
}

@Composable
private fun getStateDescription(selected: Boolean, highlighted: Boolean, disabled: Boolean): String {
    return stringResource(when {
        selected -> R.string.number_ball_state_selected
        highlighted -> R.string.number_ball_state_highlighted
        disabled -> R.string.number_ball_state_disabled
        else -> R.string.number_ball_state_available
    })
}