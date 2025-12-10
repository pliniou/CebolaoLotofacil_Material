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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
            data = stats.mostFrequentNumbers.map { it.number to it.frequency }.take(6).toImmutableList(), // Limit to 6 for 2 rows of 3
            highlightColor = MaterialTheme.colorScheme.tertiary
        )

        // Cold Numbers
        StatRow(
            title = stringResource(R.string.home_overdue_numbers),
            data = stats.mostOverdueNumbers.map { it.number to it.frequency }.take(6).toImmutableList(),
            highlightColor = MaterialTheme.colorScheme.error,
            isOverdue = true
        )
        
        // Legend
        LegendFooter()
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            data.forEach { (number, value) ->
                StatItem(number, value, highlightColor, isOverdue, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(number: Int, value: Int, highlightColor: androidx.compose.ui.graphics.Color, isOverdue: Boolean, modifier: Modifier) {
     Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(50.dp) // Fixed height for consistency
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            NumberBall(
                number = number,
                sizeVariant = NumberBallSize.Small, // Smaller ball
                variant = NumberBallVariant.Neutral
            )
            
            Column(horizontalAlignment = Alignment.End) {
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
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LegendFooter() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Dimen.SmallPadding),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
             text = "freq = vezes sorteado | atraso = concursos sem sair",
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
             textAlign = TextAlign.Center
        )
    }
}