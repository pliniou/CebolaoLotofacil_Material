package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.Formatters

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LastDrawCard(
    draw: HistoricalDraw,
    winnerData: List<WinnerData>,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
                maxItemsInEachRow = AppConfig.UI.NUMBER_GRID_ITEMS_PER_ROW
            ) {
                draw.numbers.sorted().forEach { NumberBall(it, sizeVariant = NumberBallSize.Medium, variant = NumberBallVariant.Neutral) }
            }

            if (winnerData.isNotEmpty()) {
                AppDivider()
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                    Text(stringResource(R.string.home_winners_last_contest), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    winnerData.take(3).forEach { WinnerRow(it) }
                }
            }
        }
    }
}

@Composable private fun WinnerRow(data: WinnerData) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(stringResource(R.string.home_hits_format, data.hits), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Column(horizontalAlignment = Alignment.End) {
            Text(Formatters.formatCurrency(data.prize), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(if (data.winnerCount == 1) stringResource(R.string.home_winner_count_one, data.winnerCount) else stringResource(R.string.home_winner_count_other, data.winnerCount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}