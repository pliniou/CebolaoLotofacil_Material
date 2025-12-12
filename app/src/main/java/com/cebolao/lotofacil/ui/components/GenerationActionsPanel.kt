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
    state: GenerationUiState,
    onGenerate: (Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var index by remember { mutableIntStateOf(0) }
    val quantity = AppConfig.UI.GAME_QUANTITY_OPTIONS[index]
    val isLoading = state is GenerationUiState.Loading
    val formatter = rememberCurrencyFormatter()
    val totalCost = LotofacilConstants.GAME_COST.multiply(quantity.toBigDecimal())

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = Dimen.Elevation.Low,
        shadowElevation = Dimen.Elevation.Medium
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    horizontal = Dimen.ScreenPadding,
                    vertical = Dimen.Spacing.Small
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    QuantityAdjustButton(
                        icon = Icons.Filled.Remove,
                        enabled = index > 0 && !isLoading
                    ) {
                        index--
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = Dimen.SmallPadding)
                    )

                    QuantityAdjustButton(
                        icon = Icons.Filled.Add,
                        enabled = index < AppConfig.UI.GAME_QUANTITY_OPTIONS.lastIndex && !isLoading
                    ) {
                        index++
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }

                Text(
                    text = formatter.format(totalCost),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.weight(1f),
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
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(R.string.general_cancel)
                        )
                    }
                }

                val primaryText = when {
                    !isLoading -> stringResource(R.string.filters_button_generate)
                    state.total > 0 ->
                        stringResource(
                            R.string.filters_button_generating_progress,
                            state.progress,
                            state.total
                        )

                    else -> stringResource(state.messageRes)
                }

                PrimaryActionButton(
                    text = primaryText,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    isLoading = isLoading,
                    onClick = { onGenerate(quantity) },
                    icon = {
                        if (!isLoading) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuantityAdjustButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(Dimen.LargeIcon),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimen.SmallIcon)
        )
    }
}
