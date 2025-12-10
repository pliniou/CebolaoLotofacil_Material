package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LastDrawCard(
    draw: HistoricalDraw,
    winnerData: List<WinnerData>,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier, title = null) { // Remove default separate title to control hierarchy manually
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding)) {
            // Header: Title + Contest Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_last_contest_format, draw.contestNumber),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = draw.date ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Body: Numbers (Center Stage)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
                maxItemsInEachRow = AppConfig.UI.NUMBER_GRID_ITEMS_PER_ROW
            ) {
                draw.numbers.sorted().forEach { 
                    NumberBall(it, sizeVariant = NumberBallSize.Medium, variant = NumberBallVariant.Neutral) 
                }
            }

            if (winnerData.isNotEmpty()) {
                AppDivider()
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                    winnerData.forEach { WinnerRow(it) }
                }
            }
        }
    }
}

@Composable private fun WinnerRow(data: WinnerData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_hits_format, data.hits),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (data.winnerCount == 1) 
                    stringResource(R.string.home_winner_count_one, data.winnerCount) 
                else 
                    stringResource(R.string.home_winner_count_other, data.winnerCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = Formatters.formatCurrency(data.prize),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}