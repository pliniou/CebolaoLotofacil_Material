package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.StatisticsReport // Import Corrigido
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun StatisticsPanel(
    stats: StatisticsReport,
    modifier: Modifier = Modifier,
    onTimeWindowSelected: (Int) -> Unit,
    selectedWindow: Int,
    isStatsLoading: Boolean
) {
    SectionCard(modifier = modifier.fillMaxWidth()) {
        Box {
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(isStatsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 4.dp
                        )
                    }
                }
                TimeWindowSelector(
                    selectedWindow = selectedWindow,
                    onTimeWindowSelected = onTimeWindowSelected
                )
                AppDivider()
                AnimatedContent(
                    targetState = stats,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "StatsContent"
                ) { targetStats ->
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing)) {
                        StatRow(
                            title = stringResource(R.string.home_overdue_numbers),
                            numbers = targetStats.mostOverdueNumbers.toImmutableList(),
                            icon = Icons.Default.HourglassEmpty,
                            suffix = stringResource(R.string.home_suffix_ago)
                        )
                        AppDivider()
                        StatRow(
                            title = stringResource(R.string.home_hot_numbers),
                            numbers = targetStats.mostFrequentNumbers.toImmutableList(),
                            icon = Icons.Default.LocalFireDepartment,
                            suffix = stringResource(R.string.home_suffix_times)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeWindowSelector(
    selectedWindow: Int,
    onTimeWindowSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Text(
            stringResource(R.string.home_analysis_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
            items(AppConfig.UI.TIME_WINDOWS) { window ->
                val label = if (window == 0) {
                    stringResource(R.string.home_all_contests)
                } else {
                    stringResource(R.string.home_last_contests_format, window)
                }
                TimeWindowChip(
                    isSelected = window == selectedWindow,
                    onClick = { onTimeWindowSelected(window) },
                    label = label
                )
            }
        }
    }
}