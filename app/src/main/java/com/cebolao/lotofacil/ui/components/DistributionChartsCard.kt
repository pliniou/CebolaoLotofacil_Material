package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun DistributionChartsCard(
    stats: StatisticsReport,
    selectedPattern: StatisticPattern,
    onPatternSelected: (StatisticPattern) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartData = remember(selectedPattern, stats) {
        prepareChartData(stats, selectedPattern)
    }

    val maxValue = remember(chartData) { chartData.maxOfOrNull { it.second } ?: 0 }

    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
            TitleWithIcon(
                text = "Distribuição de Padrões", 
                iconVector = selectedPattern.icon
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
            ) {
                items(StatisticPattern.entries.toTypedArray()) { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { onPatternSelected(pattern) },
                        label = { Text(pattern.title) },
                        leadingIcon = { 
                            if (selectedPattern == pattern) {
                                Icon(imageVector = pattern.icon, contentDescription = null)
                            }
                        }
                    )
                }
            }

            BarChart(
                data = chartData.toImmutableList(),
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimen.BarChartHeight)
            )
        }
    }
}

private fun prepareChartData(
    stats: StatisticsReport,
    pattern: StatisticPattern
): List<Pair<String, Int>> {
    val rawMap: Map<Int, Int> = when (pattern) {
        StatisticPattern.SUM -> stats.sumDistribution
        StatisticPattern.EVENS -> stats.evenDistribution
        StatisticPattern.PRIMES -> stats.primeDistribution
        StatisticPattern.FRAME -> stats.frameDistribution
        StatisticPattern.PORTRAIT -> stats.portraitDistribution
        StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
        StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
    }

    return if (pattern == StatisticPattern.SUM) {
        val min = AppConfig.UI.SUM_MIN_RANGE
        val max = AppConfig.UI.SUM_MAX_RANGE
        val step = AppConfig.UI.SUM_STEP
        val buckets = (min..max step step).associateWith { 0 }.toMutableMap()

        rawMap.forEach { (value, count) ->
            val bucket = (value / step) * step
            if (buckets.containsKey(bucket)) {
                buckets[bucket] = (buckets[bucket] ?: 0) + count
            }
        }
        buckets.entries
            .map { it.key.toString() to it.value }
            .sortedBy { it.first.toIntOrNull() ?: 0 }
    } else {
        rawMap.entries
            .map { it.key.toString() to it.value }
            .sortedBy { it.first.toIntOrNull() ?: 0 }
    }
}