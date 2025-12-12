package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CustomChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        modifier = modifier.height(32.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorScheme.primary,
            selectedLabelColor = colorScheme.onPrimary,
            containerColor = colorScheme.surfaceContainerHigh,
            labelColor = colorScheme.onSurfaceVariant,
            disabledContainerColor = colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
            disabledLabelColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colorScheme.outlineVariant.copy(alpha = 0.0f),
            selectedBorderColor = colorScheme.outlineVariant.copy(alpha = 0.0f)
        )
    )
}
