package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    ),
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled && !loading,
        onClick = onClick,
        shape = MaterialTheme.shapes.small, // Botão um pouco mais quadrado que os cards para distinção tátil
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Dimen.Elevation.Low,
            pressedElevation = Dimen.Elevation.None,
            disabledElevation = Dimen.Elevation.None
        )
    ) {
        AnimatedContent(
            targetState = loading,
            label = "ActionButtonContent",
            contentAlignment = Alignment.Center
        ) { isLoading ->
            Row(
                modifier = Modifier.padding(horizontal = Dimen.SmallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SmallIcon),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimen.ProgressBarStroke
                    )
                    Spacer(Modifier.width(Dimen.MediumPadding))
                } else if (leading != null) {
                    leading()
                    Spacer(Modifier.width(Dimen.SmallPadding))
                }
                
                content()
                
                if (!isLoading && trailing != null) {
                    Spacer(Modifier.width(Dimen.SmallPadding))
                    trailing()
                }
            }
        }
    }
}