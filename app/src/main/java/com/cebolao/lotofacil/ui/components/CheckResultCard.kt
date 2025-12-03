package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CheckResultCard(
    result: CheckResult,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        contentSpacing = Dimen.MediumPadding
    ) {
        val totalWins = result.scoreCounts.values.sum()
        ResultHeader(totalWins = totalWins, contestsChecked = result.lastCheckedContest)

        AppDivider()

        // NOVO: Gráfico de Desempenho Recente
        if (result.recentHits.isNotEmpty()) {
            RecentPerformanceChart(result.recentHits)
            AppDivider()
        }

        if (totalWins > 0) {
            ScoreBreakdown(result.scoreCounts)
            AppDivider()
            LastHitInfo(result)
        } else {
            NoWinsMessage()
        }
    }
}

@Composable
private fun ResultHeader(totalWins: Int, contestsChecked: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val hasWins = totalWins > 0
    val icon = if (hasWins) Icons.Filled.Celebration else Icons.Filled.Analytics
    val color = if (hasWins) colorScheme.primary else colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(Dimen.LargeIcon)
        )
        Column {
            Text(
                text = if (hasWins) stringResource(R.string.checker_results_header_wins)
                else stringResource(R.string.checker_results_header_no_wins),
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.checker_results_analysis_in_contests, contestsChecked),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentPerformanceChart(recentHits: List<Pair<Int, Int>>) {
    // Prepara os dados para o gráfico: Rótulo = Concurso, Valor = Acertos
    // Mapeamos apenas os últimos 15-20 para caber bem na tela
    val chartData = remember(recentHits) {
        recentHits.map { (contest, hits) ->
            contest.toString() to hits
        }.toImmutableList()
    }

    // Define o máximo fixo como 15 (máximo possível na Lotofácil) para manter a escala visual correta
    val maxValue = LotofacilConstants.GAME_SIZE

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(Dimen.SmallIcon)
            )
            Text(
                text = stringResource(R.string.checker_recent_hits_chart_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        BarChart(
            data = chartData,
            maxValue = maxValue,
            modifier = Modifier.fillMaxWidth(),
            // Altura reduzida para não ocupar muito espaço no card de resumo
            chartHeight = 140.dp 
        )
    }
}

@Composable
private fun ScoreBreakdown(scoreCounts: ImmutableMap<Int, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        val scores = scoreCounts.entries.sortedByDescending { it.key }
        
        scores.forEach { (score, count) ->
            if (score >= LotofacilConstants.MIN_PRIZE_SCORE) {
                ScoreRow(score = score, count = count)
            }
        }
    }
}

@Composable
private fun ScoreRow(score: Int, count: Int) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(AppConfig.Animation.LONG_DURATION),
        label = "scoreCount-$score"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.checker_score_breakdown_hits_format, score),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$animatedCount",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = StackSans,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = " ${if (animatedCount == 1) stringResource(R.string.checker_score_breakdown_times_format_one) else stringResource(R.string.checker_score_breakdown_times_format_other)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LastHitInfo(result: CheckResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(Dimen.SmallIcon)
        )
        Text(
            text = stringResource(
                R.string.checker_last_hit_info,
                result.lastHitContest?.toString() ?: DEFAULT_PLACEHOLDER,
                result.lastHitScore?.toString() ?: DEFAULT_PLACEHOLDER
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoWinsMessage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.MediumPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimen.MediumIcon)
        )
        Text(
            text = stringResource(R.string.checker_no_wins_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = Dimen.MediumPadding)
        )
    }
}