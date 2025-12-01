package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.theme.AppConfig.UI.GAME_QUANTITY_OPTIONS
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.GenerationUiState

@Composable
fun GenerationActionsPanel(
    generationState: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    val quantity = GAME_QUANTITY_OPTIONS[selectedIndex]
    val currencyFormat = rememberCurrencyFormatter()
    val isLoading = generationState is GenerationUiState.Loading

    // Container elevado (Tonal Elevation) típico de Bottom Sheets/Bars MD3
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp, // Sombra suave
        tonalElevation = 3.dp,  // Cor tonal baseada no Primary
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
        ) {
            // Seletor de Quantidade
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                QuantitySelector(
                    quantity = quantity,
                    onDecrement = {
                        if (selectedIndex > 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex--
                        }
                    },
                    onIncrement = {
                        if (selectedIndex < GAME_QUANTITY_OPTIONS.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex++
                        }
                    },
                    isDecrementEnabled = selectedIndex > 0 && !isLoading,
                    isIncrementEnabled = selectedIndex < GAME_QUANTITY_OPTIONS.lastIndex && !isLoading
                )
                Text(
                    text = currencyFormat.format(LotofacilConstants.GAME_COST.multiply(quantity.toBigDecimal())),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ações Principais
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                if (isLoading) {
                    FilledIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCancel()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Filled.Cancel, stringResource(R.string.filters_button_cancel_description))
                    }
                }

                val buttonText = if (isLoading) {
                    if (generationState.total > 0) 
                        stringResource(R.string.filters_button_generating_progress, generationState.progress, generationState.total)
                    else 
                        stringResource(generationState.messageRes)
                } else {
                    stringResource(R.string.filters_button_generate)
                }

                PrimaryActionButton(
                    text = buttonText,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    isLoading = isLoading,
                    onClick = { onGenerate(quantity) },
                    icon = { if (!isLoading) Icon(Icons.AutoMirrored.Filled.Send, null) }
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    isDecrementEnabled: Boolean,
    isIncrementEnabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Mais compacto
    ) {
        // Botões menores para o seletor
        FilledIconButton(
            onClick = onDecrement, 
            enabled = isDecrementEnabled,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(Icons.Filled.Remove, stringResource(R.string.filters_quantity_decrease), modifier = Modifier.size(16.dp))
        }
        
        Text(
            text = quantity.toString(), 
            style = MaterialTheme.typography.headlineLarge, // Numérico Grande
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        FilledIconButton(
            onClick = onIncrement, 
            enabled = isIncrementEnabled,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Icon(Icons.Filled.Add, stringResource(R.string.filters_quantity_increase), modifier = Modifier.size(16.dp))
        }
    }
}