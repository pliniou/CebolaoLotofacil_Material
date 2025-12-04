package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.ui.theme.*

@Composable
fun FilterCard(
    state: FilterState,
    onToggle: (Boolean) -> Unit,
    onRange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfo: () -> Unit,
    lastDraw: Set<Int>?,
    modifier: Modifier = Modifier
) {
    val missingData by remember(state.type, lastDraw) { derivedStateOf { state.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && lastDraw == null } }
    val active = state.isEnabled && !missingData
    
    SectionCard(modifier, backgroundColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(Dimen.SmallPadding)) {
            Header(state, active, missingData, onInfo, onToggle)
            AnimatedVisibility(active, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Content(state, onRange)
            }
        }
    }
}

@Composable private fun Header(state: FilterState, active: Boolean, missing: Boolean, onInfo: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Icon(state.type.filterIcon, null, tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(Dimen.MediumIcon))
        Column(Modifier.weight(1f)) {
            Text(stringResource(state.type.titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (missing) Text(stringResource(R.string.filters_unavailable_data), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
        IconButton(onInfo) { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(state.isEnabled, onToggle, enabled = !missing, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable private fun Content(state: FilterState, onRange: (ClosedFloatingPointRange<Float>) -> Unit) {
    val alpha by animateFloatAsState(1f, label = "alpha")
    Column(Modifier.padding(top = Dimen.MediumPadding).alpha(alpha)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Dimen.SmallPadding), Arrangement.SpaceBetween) {
            Label(stringResource(R.string.filters_min_label), state.selectedRange.start.toInt())
            Label(stringResource(R.string.filters_max_label), state.selectedRange.endInclusive.toInt(), Alignment.End)
        }
        RangeSlider(
            value = state.selectedRange,
            onValueChange = onRange,
            valueRange = state.type.fullRange,
            steps = (state.type.fullRange.endInclusive - state.type.fullRange.start).toInt() - 1,
            modifier = Modifier.padding(top = Dimen.SmallPadding)
        )
    }
}

@Composable private fun Label(txt: String, value: Int, align: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = align) {
        Text(txt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$value", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamilyNumeric, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}
