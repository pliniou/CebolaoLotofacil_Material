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
import com.cebolao.lotofacil.ui.theme.AppConfig
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
    val quantity = AppConfig.UI.GAME_QUANTITY_OPTIONS[selectedIndex]
    val currencyFormat = rememberCurrencyFormatter()
    val isLoading = generationState is GenerationUiState.Loading

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = Dimen.Elevation.Medium,
        tonalElevation = Dimen.Elevation.Low,
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
                        if (selectedIndex < AppConfig.UI.GAME_QUANTITY_OPTIONS.lastIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIndex++
                        }
                    },
                    isDecrementEnabled = selectedIndex > 0 && !isLoading,
                    isIncrementEnabled = selectedIndex < AppConfig.UI.GAME_QUANTITY_OPTIONS.lastIndex && !isLoading
                )
                Text(
                    text = currencyFormat.format(LotofacilConstants.GAME_COST.multiply(quantity.toBigDecimal())),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ações Principais
            ActionButtons(
                isLoading = isLoading,
                generationState = generationState,
                onCancel = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCancel()
                },
                onGenerate = { onGenerate(quantity) },
                modifier = Modifier.weight(1f)
            )
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
        horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)
    ) {
        SelectorButton(
            icon = Icons.Default.Remove,
            descRes = R.string.filters_quantity_decrease,
            enabled = isDecrementEnabled,
            onClick = onDecrement
        )
        
        Text(
            text = quantity.toString(), 
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimen.SmallPadding)
        )
        
        SelectorButton(
            icon = Icons.Default.Add,
            descRes = R.string.filters_quantity_increase,
            enabled = isIncrementEnabled,
            onClick = onIncrement
        )
    }
}

@Composable
private fun SelectorButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descRes: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick, 
        enabled = enabled,
        modifier = Modifier.size(32.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = stringResource(descRes), 
            modifier = Modifier.size(Dimen.SmallIcon)
        )
    }
}

@Composable
private fun ActionButtons(
    isLoading: Boolean,
    generationState: GenerationUiState,
    onCancel: () -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        if (isLoading) {
            FilledIconButton(
                onClick = onCancel,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Cancel, stringResource(R.string.filters_button_cancel_description))
            }
        }

        val buttonText = if (isLoading) {
            if (generationState is GenerationUiState.Loading && generationState.total > 0) 
                stringResource(R.string.filters_button_generating_progress, generationState.progress, generationState.total)
            else if (generationState is GenerationUiState.Loading)
                stringResource(generationState.messageRes)
            else ""
        } else {
            stringResource(R.string.filters_button_generate)
        }

        PrimaryActionButton(
            text = buttonText,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            isLoading = isLoading,
            onClick = onGenerate,
            icon = { if (!isLoading) Icon(Icons.AutoMirrored.Filled.Send, null) }
        )
    }
}