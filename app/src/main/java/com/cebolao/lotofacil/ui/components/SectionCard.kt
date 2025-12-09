package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer, // Mapped to Low/High consistent with theme
    contentPadding: Dp = Dimen.CardContentPadding,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(Dimen.ItemSpacing),
    headerActions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Consistent with GameCard (16dp)
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimen.Elevation.Low),
        border = androidx.compose.foundation.BorderStroke(
            Dimen.Border.Hairline, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement
        ) {
            if (title != null || headerActions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (title != null) Dimen.SmallPadding else 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (headerActions != null) {
                        headerActions()
                    }
                }
                @Suppress("ControlFlowWithEmptyBody")
                if (title != null) {
                     // Optional: Separator below header if desired, but clean design often omits it
                     // unless explicitly needed. Keeping it clean for now.
                     // AppDivider(Modifier.padding(bottom = Dimen.SmallPadding))
                }
            }
            content()
        }
    }
}