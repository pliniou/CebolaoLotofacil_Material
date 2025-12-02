package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyNumeric
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallSize { Large, Medium, Small }
enum class NumberBallVariant { Primary, Secondary, Neutral, Hit, Miss }

@Composable
fun NumberBall(
    number: Int,
    modifier: Modifier = Modifier,
    sizeVariant: NumberBallSize = NumberBallSize.Medium,
    isSelected: Boolean = false,
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

    val colors = resolveBallColors(isSelected, variant)
    val bgColor by animateColorAsState(colors.first, label = "bgColor")
    val contentColor by animateColorAsState(colors.second, label = "contentColor")
    val borderColor = colors.third

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (isDisabled) AppConfig.UI.ALPHA_DISABLED else 1f)
            .clip(CircleShape)
            .background(bgColor)
            .then(if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = DEFAULT_NUMBER_FORMAT.format(number),
            color = contentColor,
            fontFamily = FontFamilyNumeric,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun resolveBallColors(isSelected: Boolean, variant: NumberBallVariant): Triple<Color, Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when {
        isSelected -> Triple(scheme.primary, scheme.onPrimary, Color.Transparent)
        variant == NumberBallVariant.Hit -> Triple(com.cebolao.lotofacil.ui.theme.SuccessGreen, Color.White, Color.Transparent)
        variant == NumberBallVariant.Miss -> Triple(scheme.errorContainer, scheme.onErrorContainer, Color.Transparent)
        variant == NumberBallVariant.Secondary -> Triple(scheme.secondaryContainer, scheme.onSecondaryContainer, Color.Transparent)
        variant == NumberBallVariant.Neutral -> Triple(scheme.surfaceContainerHigh, scheme.onSurface, Color.Transparent)
        else -> Triple(scheme.surface, scheme.onSurface, scheme.outlineVariant.copy(alpha = 0.5f))
    }
}