package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.StackSans
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CheckResultCard(result: CheckResult, modifier: Modifier = Modifier) {
    SectionCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        val totalWins = result.scoreCounts.values.sum()
        ResultHeader(totalWins, result.lastCheckedContest)
        
        AppDivider(modifier = Modifier.padding(vertical = Dimen.SmallPadding))

        if (totalWins > 0) {
             // Win Probability (Simple)
            val winRate = (totalWins.toFloat() / result.lastCheckedContest.toFloat()) * 100
            val winRateFormatted = "%.2f%%".format(winRate)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timeline, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(Dimen.SmallIcon).padding(end = 6.dp))
                    Text("Frequência de Prêmios", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = winRateFormatted, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
             AppDivider(modifier = Modifier.padding(vertical = Dimen.SmallPadding))
        }

        if (result.recentHits.isNotEmpty()) {
            val chartData = remember(result.recentHits) { result.recentHits.map { it.first.toString() to it.second }.toImmutableList() }
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                Text(stringResource(R.string.checker_recent_hits_chart_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                BarChart(chartData, LotofacilConstants.GAME_SIZE, Modifier.fillMaxWidth(), Dimen.CheckResultChartHeight)
            }
            AppDivider(modifier = Modifier.padding(vertical = Dimen.SmallPadding))
        }

        if (totalWins > 0) {
            result.scoreCounts.entries.sortedByDescending { it.key }.filter { it.key >= LotofacilConstants.MIN_PRIZE_SCORE }.forEach { (score, count) ->
                ScoreRow(score, count)
            }
            AppDivider(modifier = Modifier.padding(vertical = Dimen.SmallPadding))
            LastHit(result)
        } else NoWins()
    }
}

@Composable private fun ResultHeader(wins: Int, checked: Int) {
    val hasWins = wins > 0
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Icon(if (hasWins) Icons.Filled.Celebration else Icons.Filled.Analytics, null, tint = if (hasWins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(Dimen.LargeIcon))
        Column {
            Text(if (hasWins) stringResource(R.string.checker_results_header_wins) else stringResource(R.string.checker_results_header_no_wins), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (hasWins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.checker_results_analysis_in_contests, checked), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun ScoreRow(score: Int, count: Int) {
    val animCount by animateIntAsState(count, tween(AppConfig.Animation.LONG_DURATION), "count")
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(stringResource(R.string.checker_score_breakdown_hits_format, score), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$animCount", style = MaterialTheme.typography.titleLarge, fontFamily = StackSans, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(if (animCount == 1) " vez" else " vezes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun LastHit(res: CheckResult) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(Dimen.SmallIcon))
        Text(stringResource(R.string.checker_last_hit_info, res.lastHitContest?.toString() ?: DEFAULT_PLACEHOLDER, res.lastHitScore?.toString() ?: DEFAULT_PLACEHOLDER), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun NoWins() {
    Row(Modifier.fillMaxWidth().padding(vertical = Dimen.MediumPadding), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(Dimen.MediumIcon))
        Text(stringResource(R.string.checker_no_wins_message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(start = Dimen.MediumPadding))
    }
}