package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.StackSans
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameAnalysisDialog(
    result: GameAnalysisResult,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                stringResource(R.string.games_analysis_dialog_title), 
                style = MaterialTheme.typography.headlineMedium 
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                    Text(
                        stringResource(R.string.games_analysis_combination_title), 
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding),
                        maxItemsInEachRow = AppConfig.UI.NUMBER_GRID_ITEMS_PER_ROW
                    ) {
                        result.game.numbers.sorted().forEach {                            
                            NumberBall(
                                number = it,
                                sizeVariant = NumberBallSize.Medium, 
                                variant = NumberBallVariant.Primary
                            )
                        }
                    }
                }

                SimpleStatsCard(stats = result.simpleStats)

                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
                        Text(
                            stringResource(R.string.games_analysis_prize_summary_title), 
                            style = MaterialTheme.typography.titleMedium
                        )
                        AppDivider()
                        val totalPremios = result.checkResult.scoreCounts.values.sum()
                        val ultimoConcurso = result.checkResult.lastHitContest?.toString() ?: DEFAULT_PLACEHOLDER
                        val ultimoAcerto = result.checkResult.lastHitScore?.toString() ?: DEFAULT_PLACEHOLDER

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.games_analysis_total_label), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "$totalPremios", 
                                style = MaterialTheme.typography.headlineSmall, 
                                fontFamily = StackSans,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.games_analysis_last_prize_label), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                stringResource(R.string.games_analysis_last_prize_value, ultimoConcurso, ultimoAcerto),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    stringResource(id = R.string.general_close),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}