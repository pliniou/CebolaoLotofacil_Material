package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
            // Bolas Sorteadas
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
                maxItemsInEachRow = AppConfig.UI.NUMBER_GRID_ITEMS_PER_ROW
            ) {
                draw.numbers.sorted().forEach { number ->
                    NumberBall(
                        number = number,
                        sizeVariant = NumberBallSize.Medium,
                        variant = NumberBallVariant.Neutral
                    )
                }
            }

            if (winnerData.isNotEmpty()) {
                AppDivider()
                
                // Lista de Ganhadores
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                    Text(
                        text = stringResource(R.string.home_winners_last_contest),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    winnerData.take(3).forEach { winner -> // Mostrar apenas os principais
                        WinnerRow(winner)
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnerRow(winner: WinnerData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.home_hits_format, winner.hits),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = Formatters.formatCurrency(winner.prize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            val winnerCountText = if (winner.winnerCount == 1) 
                stringResource(R.string.home_winner_count_one, winner.winnerCount)
            else 
                stringResource(R.string.home_winner_count_other, winner.winnerCount)
                
            Text(
                text = winnerCountText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}