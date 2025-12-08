package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyNumeric
import com.cebolao.lotofacil.ui.theme.SuccessGreen
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

enum class NumberBallSize { Large, Medium, Small }
enum class NumberBallVariant { Primary, Secondary, Neutral, Hit, Miss }

@Immutable
private data class BallColors(val container: Color, val content: Color, val border: Color)

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

    val scheme = MaterialTheme.colorScheme
    val colors = remember(isSelected, variant, scheme) {
        resolveColors(isSelected, variant, scheme)
    }

    val bg by animateColorAsState(colors.container, label = "bg")
    val content by animateColorAsState(colors.content, label = "content")
    
    // Shadow for depth (except when disabled or neutral flat variant)
    val shadowElevation = if (!isDisabled && (isSelected || variant == NumberBallVariant.Hit)) 2.dp else 0.dp

    androidx.compose.material3.Surface(
        modifier = modifier
            .size(size)
            .alpha(if (isDisabled) AppConfig.UI.ALPHA_DISABLED else 1f),
        shape = CircleShape,
        color = bg,
        contentColor = content,
        shadowElevation = shadowElevation,
        border = if (colors.border != Color.Transparent) BorderStroke(Dimen.Border.Thin, colors.border) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = DEFAULT_NUMBER_FORMAT.format(number),
                fontFamily = FontFamilyNumeric,
                fontSize = fontSize,
                fontWeight = if (isSelected || variant == NumberBallVariant.Hit) FontWeight.Black else FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun resolveColors(
    isSelected: Boolean,
    variant: NumberBallVariant,
    scheme: ColorScheme
): BallColors {
    return when {
        isSelected -> BallColors(
            container = scheme.primary,
            content = scheme.onPrimary,
            border = Color.Transparent
        )
        variant == NumberBallVariant.Hit -> BallColors(
            container = SuccessGreen,
            content = Color.White,
            border = Color.Transparent
        )
        variant == NumberBallVariant.Miss -> BallColors(
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            border = Color.Transparent
        )
        variant == NumberBallVariant.Secondary -> BallColors(
            container = scheme.secondaryContainer,
            content = scheme.onSecondaryContainer,
            border = Color.Transparent
        )
        variant == NumberBallVariant.Neutral -> BallColors(
            container = scheme.surfaceContainerHigh,
            content = scheme.onSurface,
            border = Color.Transparent
        )
        else -> BallColors(
            container = scheme.surface,
            content = scheme.onSurface,
            border = scheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}