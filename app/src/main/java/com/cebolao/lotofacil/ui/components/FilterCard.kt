package com.cebolao.lotofacil.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyNumeric
import com.cebolao.lotofacil.ui.theme.filterIcon

@Composable
fun FilterCard(
    filterState: FilterState,
    onEnabledChange: (Boolean) -> Unit,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfoClick: () -> Unit,
    lastDrawNumbers: Set<Int>? = null, // Usado para determinar disponibilidade
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    // Lógica simples de UI derivada (rápida, sem recomposição pesada)
    val isDataMissing by remember(filterState.type, lastDrawNumbers) {
        derivedStateOf { 
            filterState.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && lastDrawNumbers == null 
        }
    }
    
    val isActive = filterState.isEnabled && !isDataMissing
    
    // Animação de opacidade
    val contentAlpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.5f, label = "alpha")

    SectionCard(
        modifier = modifier,
        backgroundColor = if(isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(Dimen.SmallPadding)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
            ) {
                Icon(
                    imageVector = filterState.type.filterIcon,
                    contentDescription = null,
                    tint = if(isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimen.MediumIcon)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(filterState.type.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isDataMissing) {
                        Text(
                            text = stringResource(R.string.filters_unavailable_data),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Outlined.Info, 
                        contentDescription = stringResource(R.string.filters_info_button_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = filterState.isEnabled,
                    onCheckedChange = onEnabledChange,
                    enabled = !isDataMissing,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Slider Content
            AnimatedVisibility(
                visible = isActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = Dimen.MediumPadding).alpha(contentAlpha)) {
                    RangeLabels(
                        start = filterState.selectedRange.start.toInt(),
                        end = filterState.selectedRange.endInclusive.toInt()
                    )
                    
                    RangeSlider(
                        value = filterState.selectedRange,
                        onValueChange = onRangeChange,
                        valueRange = filterState.type.fullRange,
                        // Steps: (Total - 1) para snap em inteiros
                        steps = (filterState.type.fullRange.endInclusive - filterState.type.fullRange.start).toInt() - 1,
                        modifier = Modifier.padding(top = Dimen.SmallPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun RangeLabels(start: Int, end: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.SmallPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ValueIndicator(stringResource(R.string.filters_min_label), start)
        ValueIndicator(stringResource(R.string.filters_max_label), end, Alignment.End)
    }
}

@Composable
private fun ValueIndicator(label: String, value: Int, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge, 
            fontFamily = FontFamilyNumeric,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}