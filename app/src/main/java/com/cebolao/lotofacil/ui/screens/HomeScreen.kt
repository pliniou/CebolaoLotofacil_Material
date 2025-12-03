package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Consumo de eventos de mensagem (Sync Success/Fail)
    uiState.syncMessageRes?.let { msgId ->
        val message = stringResource(msgId)
        LaunchedEffect(msgId) {
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = viewModel::forceSync, enabled = !uiState.isSyncing) {
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
        HomeContent(
            uiState = uiState,
            onTimeWindowSelected = viewModel::onTimeWindowSelected,
            onPatternSelected = viewModel::onPatternSelected,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun HomeContent(
    uiState: com.cebolao.lotofacil.viewmodels.HomeUiState,
    onTimeWindowSelected: (Int) -> Unit,
    onPatternSelected: (com.cebolao.lotofacil.domain.model.StatisticPattern) -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = Dimen.BottomBarSpacer)
    ) {
        Box(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)) {
            WelcomeCard()
        }

        Box(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding)) {
            when (val state = uiState.screenState) {
                is HomeScreenState.Success -> NextContestHeroCard(state.nextDrawInfo)
                is HomeScreenState.Loading -> LoadingCard()
                is HomeScreenState.Error -> { /* Error handled by Snackbar or Placeholder */ }
            }
        }

        if (uiState.screenState is HomeScreenState.Success) {
            uiState.screenState.lastDraw?.let { draw ->
                SectionHeader(stringResource(R.string.home_last_contest_format, draw.contestNumber))
                LastDrawCard(
                    draw = draw,
                    winnerData = uiState.screenState.winnerData,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
            }
        }

        SectionHeader(stringResource(R.string.home_statistics_center))

        uiState.statistics?.let { stats ->
            StatisticsPanel(
                stats = stats,
                modifier = Modifier.padding(horizontal = Dimen.ScreenPadding),
                selectedWindow = uiState.selectedTimeWindow,
                onTimeWindowSelected = onTimeWindowSelected,
                isStatsLoading = uiState.isStatsLoading
            )

            Spacer(Modifier.height(Dimen.MediumPadding))

            DistributionChartsCard(
                stats = stats,
                selectedPattern = uiState.selectedPattern,
                onPatternSelected = onPatternSelected,
                modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
            )
        } ?: if (uiState.isStatsLoading) LoadingStats() else Unit

        Spacer(Modifier.height(Dimen.SectionSpacing))
        InfoTipCard(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding))
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
            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Dimen.MediumPadding))
            Text(
                text = stringResource(R.string.general_disclaimer_responsibility),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}