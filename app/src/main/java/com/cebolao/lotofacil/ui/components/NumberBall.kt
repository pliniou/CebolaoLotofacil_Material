package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallSize { Large, Medium, Small }
enum class NumberBallVariant { Primary, Secondary, Neutral }

@Immutable
private data class BallStyle(
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val useGradient: Boolean = false,
    val borderWidth: androidx.compose.ui.unit.Dp
)

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    sizeVariant: NumberBallSize = NumberBallSize.Large,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isDisabled: Boolean = false,
    variant: NumberBallVariant = NumberBallVariant.Primary
) {
    val size = when (sizeVariant) {
        NumberBallSize.Large -> Dimen.BallSizeLarge
        NumberBallSize.Medium -> Dimen.BallSizeMedium
        NumberBallSize.Small -> Dimen.BallSizeSmall
    }
    
    val fontSize = when (sizeVariant) {
        NumberBallSize.Large -> Dimen.BallTextLarge
        NumberBallSize.Medium -> Dimen.BallTextMedium
        NumberBallSize.Small -> Dimen.BallTextSmall
    }

    val targetScale = if (isSelected) 1.15f else 1f
    val scale by animateFloatAsState(targetValue = targetScale, label = "scale")

    val style = resolveBallStyle(isSelected, isHighlighted, isDisabled, variant)
    
    val animatedBg by animateColorAsState(style.backgroundColor, label = "bg")
    val animatedBorder by animateColorAsState(style.borderColor, label = "border")
    val animatedContent by animateColorAsState(style.contentColor, label = "content")

    val contentDesc = stringResource(
        R.string.number_ball_content_description, 
        number, 
        if(isSelected) stringResource(R.string.general_selected) else ""
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (style.useGradient) {
                    Brush.radialGradient(
                        colors = listOf(animatedBg.copy(alpha = 0.8f), animatedBg),
                        radius = 100f
                    )
                } else {
                    androidx.compose.ui.graphics.SolidColor(animatedBg)
                }
            )
            .border(width = style.borderWidth, color = animatedBorder, shape = CircleShape)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DEFAULT_NUMBER_FORMAT.format(number),
            color = animatedContent,
            style = MaterialTheme.typography.labelLarge,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun resolveBallStyle(
    isSelected: Boolean,
    isHighlighted: Boolean,
    isDisabled: Boolean,
    variant: NumberBallVariant
): BallStyle {
    val colorScheme = MaterialTheme.colorScheme

    return when {
        isDisabled -> BallStyle(
            backgroundColor = colorScheme.surfaceContainer.copy(alpha = 0.3f),
            contentColor = colorScheme.onSurface.copy(alpha = 0.2f),
            borderColor = Color.Transparent,
            borderWidth = 1.dp
        )
        isSelected -> BallStyle(
            backgroundColor = if(variant == NumberBallVariant.Secondary) colorScheme.secondary else colorScheme.primary,
            contentColor = if(variant == NumberBallVariant.Secondary) colorScheme.onSecondary else colorScheme.onPrimary,
            borderColor = if(variant == NumberBallVariant.Secondary) colorScheme.secondaryContainer else colorScheme.primaryContainer,
            useGradient = true,
            borderWidth = 2.dp
        )
        isHighlighted -> BallStyle(
            backgroundColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            borderColor = colorScheme.tertiary,
            borderWidth = 1.dp
        )
        else -> BallStyle(
            backgroundColor = colorScheme.surfaceContainerLow,
            contentColor = colorScheme.onSurface,
            borderColor = colorScheme.outlineVariant.copy(alpha = 0.3f),
            borderWidth = 1.dp
        )
    }
}