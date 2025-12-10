package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    hasBorder: Boolean = true, // Default to true for better separation in Flat UI
    elevation: Dp = Dimen.Elevation.None, // No elevation by default
    contentPadding: Dp = Dimen.CardContentPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = Shapes.medium // Standard 12dp
    
    // Subtle border for definition
    val border = if (hasBorder) {
        BorderStroke(Dimen.Border.Hairline, MaterialTheme.colorScheme.outlineVariant)
    } else null

    val colors = CardDefaults.cardColors(containerColor = backgroundColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border
        ) {
            Column(Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = cardElevation,
            border = border
        ) {
            Column(Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}
