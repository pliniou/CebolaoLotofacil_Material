package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.StatisticsReport
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun StatisticsPanel(
    stats: StatisticsReport,
    modifier: Modifier = Modifier,
    onTimeWindowSelected: (Int) -> Unit,
    selectedWindow: Int,
    isStatsLoading: Boolean
) {
    SectionCard(
        modifier = modifier,
        title = stringResource(R.string.home_statistics_center),
        headerActions = {
             AnimatedVisibility(
                visible = isStatsLoading,
                enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimen.SmallIcon),
                    strokeWidth = Dimen.Border.Thin,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        contentPadding = Dimen.CardContentPadding
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding),
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            // Selector
            TimeWindowSelector(
                selectedWindow = selectedWindow,
                onTimeWindowSelected = onTimeWindowSelected,
                enabled = !isStatsLoading
            )

            AppDivider()

            // Content with Animation
            AnimatedContent(
                targetState = isStatsLoading,
                transitionSpec = {
                    if (targetState) {
                        (androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically())
                            .togetherWith(androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically())
                    } else {
                        (androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically())
                            .togetherWith(androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically())
                    }.using(SizeTransform(clip = false))
                },
                label = "StatsContentTransition"
            ) { loading ->
                if (!loading) {
                    StatsContent(stats = stats)
                } else {
                    Spacer(modifier = Modifier.height(Dimen.SectionSpacing))
                }
            }
        }
    }
}



@Composable
private fun TimeWindowSelector(
    selectedWindow: Int,
    onTimeWindowSelected: (Int) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Text(
            stringResource(R.string.home_analysis_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (enabled) 1f else 0.6f
            )
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = AppConfig.UI.TIME_WINDOWS, key = { it }) { window ->
                val label = when (window) {
                    0 -> stringResource(R.string.home_all_contests)
                    else -> stringResource(R.string.home_last_contests_format, window)
                }
                TimeWindowChip(
                    isSelected = window == selectedWindow,
                    onClick = { onTimeWindowSelected(window) },
                    label = label,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
private fun StatsContent(stats: StatisticsReport) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing)) {
        // Hot Numbers
        StatRow(
            title = stringResource(R.string.home_hot_numbers),
            data = stats.mostFrequentNumbers.map { it.number to it.frequency }.toImmutableList(),
            highlightColor = MaterialTheme.colorScheme.tertiary
        )

        // Cold Numbers
        StatRow(
            title = stringResource(R.string.home_overdue_numbers),
            data = stats.mostOverdueNumbers.map { it.number to it.frequency }.toImmutableList(),
            highlightColor = MaterialTheme.colorScheme.error,
            isOverdue = true
        )
    }
}

@Composable
private fun StatRow(
    title: String,
    data: ImmutableList<Pair<Int, Int>>,
    highlightColor: androidx.compose.ui.graphics.Color,
    isOverdue: Boolean = false
) {
    if (data.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = data,
                key = { it.first }
            ) { (number, value) ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.width(56.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        NumberBall(
                            number = number,
                            sizeVariant = NumberBallSize.Medium,
                            variant = NumberBallVariant.Neutral
                        )
                        
                        Text(
                            text = value.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            color = highlightColor
                        )
                        
                        Text(
                            text = if (isOverdue) "atraso" else "freq",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}