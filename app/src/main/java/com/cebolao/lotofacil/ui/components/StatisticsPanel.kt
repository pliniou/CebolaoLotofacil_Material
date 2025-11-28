package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.StatisticsReport
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
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
                Text(
                    text = stringResource(id = R.string.home_statistics_center),
                    style = MaterialTheme.typography.titleLarge
                )
                TimeWindowSelector(
                    selectedWindow = selectedWindow,
                    onTimeWindowSelected = onTimeWindowSelected
                )

                AppDivider()

                AnimatedContent(
                    targetState = stats,
                    transitionSpec = { fadeIn(tween(AppConfig.Animation.MEDIUM_DURATION)) togetherWith fadeOut(tween(AppConfig.Animation.MEDIUM_DURATION)) },
                    label = "StatsContent"
                ) { targetStats ->
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
                        StatRow(
                            title = stringResource(R.string.home_hot_numbers),
                            numbers = targetStats.mostFrequentNumbers.toImmutableList(),
                            icon = Icons.Default.LocalFireDepartment,
                            suffix = stringResource(R.string.home_suffix_times)
                        )
                        AppDivider()
                        StatRow(
                            title = stringResource(R.string.home_overdue_numbers),
                            numbers = targetStats.mostOverdueNumbers.toImmutableList(),
                            icon = Icons.Default.HourglassEmpty,
                            suffix = stringResource(R.string.home_suffix_ago)
                        )
                    }
                }
            }

            if (isStatsLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AppConfig.UI.ALPHA_BORDER_DEFAULT))
                ) {
                    CircularProgressIndicator()
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
        Text(stringResource(R.string.home_analysis_period), style = MaterialTheme.typography.titleMedium)
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