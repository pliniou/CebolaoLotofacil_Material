package com.cebolao.lotofacil.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TimeWindowChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String,
    enabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    CustomChip(
        selected = isSelected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled
    )
}