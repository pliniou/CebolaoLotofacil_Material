package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.GenerationUiState

@Composable
fun GenerationActionsPanel(
    state: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var idx by remember { mutableIntStateOf(0) }
    val qty = AppConfig.UI.GAME_QUANTITY_OPTIONS[idx]
    val loading = state is GenerationUiState.Loading
    val formatter = rememberCurrencyFormatter()

    Surface(modifier.fillMaxWidth(), shadowElevation = Dimen.Elevation.Medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.LargePadding)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
                    Btn(Icons.Default.Remove, idx > 0 && !loading) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); idx-- }
                    Text("$qty", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = Dimen.SmallPadding))
                    Btn(Icons.Default.Add, idx < AppConfig.UI.GAME_QUANTITY_OPTIONS.lastIndex && !loading) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); idx++ }
                }
                Text(formatter.format(LotofacilConstants.GAME_COST.multiply(qty.toBigDecimal())), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                if (loading) FilledIconButton(onCancel, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)) { Icon(Icons.Default.Cancel, null) }
                PrimaryActionButton(
                    text = if (loading) (if (state.total > 0) stringResource(R.string.filters_button_generating_progress, state.progress, state.total) else stringResource(state.messageRes)) else stringResource(R.string.filters_button_generate),
                    modifier = Modifier.weight(1f),
                    enabled = !loading,
                    isLoading = loading,
                    onClick = { onGenerate(qty) },
                    icon = { if (!loading) Icon(Icons.AutoMirrored.Filled.Send, null) }
                )
            }
        }
    }
}

@Composable private fun Btn(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    FilledIconButton(onClick, enabled = enabled, modifier = Modifier.size(Dimen.LargeIcon), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) { Icon(icon, null, Modifier.size(Dimen.SmallIcon)) }
}