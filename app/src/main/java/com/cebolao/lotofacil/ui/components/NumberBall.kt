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
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallVariant { Primary, Secondary, Lotofacil }

@Immutable
private data class BallStyle(
    val gradientStart: Color,
    val gradientEnd: Color,
    val content: Color,
    val border: Color,
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

    val elevation by animateDpAsState(style.elevation, label = "elevation")
    val startColor by animateColorAsState(style.gradientStart, tween(AppConfig.Animation.SHORT_DURATION), label = "start")
    val endColor by animateColorAsState(style.gradientEnd, tween(AppConfig.Animation.SHORT_DURATION), label = "end")
    val contentColor by animateColorAsState(style.content, tween(AppConfig.Animation.SHORT_DURATION), label = "content")
    val borderColor by animateColorAsState(style.border, tween(AppConfig.Animation.SHORT_DURATION), label = "border")

    val desc = stringResource(
        R.string.number_ball_content_description, 
        number, 
        getStateDescription(isSelected, isHighlighted, isDisabled)
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(startColor, endColor)))
            .border(
                width = if (isSelected) Dimen.Border.Thick else Dimen.Border.Default,
                color = borderColor,
                shape = CircleShape
            )
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DEFAULT_NUMBER_FORMAT.format(number),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (size.value / AppConfig.UI.BALL_TEXT_FACTOR).sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
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
            gradientStart = theme.surfaceVariant.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
            gradientEnd = theme.surface.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
            content = theme.onSurfaceVariant.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
            border = theme.outline.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
            elevation = Dimen.Elevation.Level0
        )
        isSelected -> BallStyle(
            gradientStart = baseColor,
            gradientEnd = baseColor.copy(alpha = AppConfig.UI.ALPHA_SELECTED),
            content = onBaseColor,
            border = baseColor.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
            elevation = Dimen.Elevation.Level3
        )
        isHighlighted -> BallStyle(
            gradientStart = baseColor.copy(alpha = AppConfig.UI.ALPHA_HIGHLIGHT),
            gradientEnd = baseColor.copy(alpha = 0.1f),
            content = baseColor,
            border = baseColor.copy(alpha = 0.6f),
            elevation = Dimen.Elevation.Level1
        )
        else -> BallStyle(
            gradientStart = theme.surface,
            gradientEnd = theme.surfaceVariant,
            content = theme.onSurfaceVariant,
            border = theme.outline.copy(alpha = AppConfig.UI.ALPHA_BORDER_DEFAULT),
            elevation = Dimen.Elevation.Level1
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