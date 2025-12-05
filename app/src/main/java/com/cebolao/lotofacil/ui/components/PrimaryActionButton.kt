package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    isFullWidth: Boolean = true
) {
    val widthModifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(Dimen.LargeButtonHeight)
            .then(widthModifier)
            .bounceClick(scaleDown = 0.97f),
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Dimen.Elevation.Low,
            pressedElevation = 0.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { 
                fadeIn(tween(AppConfig.Animation.SHORT_DURATION)) togetherWith 
                fadeOut(tween(AppConfig.Animation.SHORT_DURATION)) 
            },
            label = "ButtonContent"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.ActionIconSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = Dimen.Border.Thick
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(Dimen.ItemSpacing))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}