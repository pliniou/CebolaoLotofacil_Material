package com.cebolao.lotofacil.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.ui.theme.Dimen

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