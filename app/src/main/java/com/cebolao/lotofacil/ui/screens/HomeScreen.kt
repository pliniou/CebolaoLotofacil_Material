package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.LastDrawCard
import com.cebolao.lotofacil.ui.components.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.StatisticsPanel
import com.cebolao.lotofacil.ui.components.WelcomeCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeViewModel

private const val DISCLAIMER_TEXT = "Lembre-se: Probabilidade não é certeza. Jogue com responsabilidade."

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        actions = {
            IconButton(onClick = viewModel::forceSync) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SmallIcon),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        stringResource(R.string.home_sync_button_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = Dimen.BottomBarSpacer)
        ) {
            Box(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)) {
                WelcomeCard()
            }

            Box(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding)) {
                when (val state = uiState.screenState) {
                    is HomeScreenState.Success -> NextContestHeroCard(state.nextDrawInfo)
                    else -> LoadingCard()
                }
            }

            if (uiState.screenState is HomeScreenState.Success) {
                val state = uiState.screenState as HomeScreenState.Success
                state.lastDraw?.let { draw ->
                    SectionHeader(title = stringResource(R.string.home_last_contest_format, draw.contestNumber))
                    LastDrawCard(
                        draw = draw,
                        winnerData = state.winnerData,
                        modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                    )
                }
            }

            SectionHeader(title = stringResource(R.string.home_statistics_center))

            uiState.statistics?.let { stats ->
                StatisticsPanel(
                    stats = stats,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding),
                    selectedWindow = uiState.selectedTimeWindow,
                    onTimeWindowSelected = viewModel::onTimeWindowSelected,
                    isStatsLoading = uiState.isStatsLoading
                )

                Spacer(Modifier.height(Dimen.MediumPadding))

                DistributionChartsCard(
                    stats = stats,
                    selectedPattern = uiState.selectedPattern,
                    onPatternSelected = viewModel::onPatternSelected,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
            } ?: LoadingStats()

            Spacer(Modifier.height(Dimen.SectionSpacing))
            InfoTipCard(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(
            start = Dimen.ScreenPadding,
            top = Dimen.LargePadding,
            bottom = Dimen.SmallPadding
        )
    )
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadingStats() {
    Column(Modifier.padding(Dimen.ScreenPadding)) {
        LinearProgressIndicator(Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.general_loading_analysis),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Dimen.SmallPadding),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoTipCard(modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Info,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Dimen.MediumPadding))
            Text(
            	text = stringResource(R.string.general_disclaimer_responsibility),
            	style = MaterialTheme.typography.bodyMedium,
            	color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}