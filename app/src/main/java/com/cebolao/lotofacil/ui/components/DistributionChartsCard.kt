package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
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
    val chartData = remember(selectedPattern, stats) { prepareData(stats, selectedPattern) }
    val maxValue = remember(chartData) { chartData.maxOfOrNull { it.second } ?: 0 }

    SectionCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding)) {
            TitleWithIcon(stringResource(R.string.stats_distribution_title_format, selectedPattern.title), iconVector = selectedPattern.icon)
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                items(
                    items = StatisticPattern.entries.toTypedArray(),
                    key = { it.name }
                ) { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { onPatternSelected(pattern) },
                        label = { Text(pattern.title) },
                        leadingIcon = { if (selectedPattern == pattern) Icon(pattern.icon, null) }
                    )
                }
            }

            BarChart(chartData.toImmutableList(), maxValue, Modifier.fillMaxWidth().height(Dimen.BarChartHeight))
        }
    }
}

private fun prepareData(stats: StatisticsReport, pattern: StatisticPattern): List<Pair<String, Int>> {
    val raw = when (pattern) {
        StatisticPattern.SUM -> stats.sumDistribution
        StatisticPattern.EVENS -> stats.evenDistribution
        StatisticPattern.PRIMES -> stats.primeDistribution
        StatisticPattern.FRAME -> stats.frameDistribution
        StatisticPattern.PORTRAIT -> stats.portraitDistribution
        StatisticPattern.FIBONACCI -> stats.fibonacciDistribution
        StatisticPattern.MULTIPLES_OF_3 -> stats.multiplesOf3Distribution
    }

    if (pattern == StatisticPattern.SUM) {
        val buckets = (AppConfig.UI.SUM_MIN_RANGE..AppConfig.UI.SUM_MAX_RANGE step AppConfig.UI.SUM_STEP).associateWith { 0 }.toMutableMap()
        raw.forEach { (k, v) -> if (buckets.containsKey(k)) buckets[k] = v }
        return buckets.entries.sortedBy { it.key }.map { it.key.toString() to it.value }
    }
    return raw.entries.sortedBy { it.key }.map { it.key.toString() to it.value }
}