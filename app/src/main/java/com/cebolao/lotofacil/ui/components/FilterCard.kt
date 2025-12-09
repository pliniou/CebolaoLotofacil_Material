package com.cebolao.lotofacil.ui.components

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
import com.cebolao.lotofacil.ui.theme.FontFamilyDisplay
import com.cebolao.lotofacil.ui.theme.filterIcon

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
    
    SectionCard(modifier.fillMaxWidth(), backgroundColor = if (active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(Dimen.MediumPadding)) { // Increased padding
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
    Column(Modifier.padding(top = Dimen.SmallPadding).alpha(alpha)) {
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
        Text("$value", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamilyDisplay, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}
