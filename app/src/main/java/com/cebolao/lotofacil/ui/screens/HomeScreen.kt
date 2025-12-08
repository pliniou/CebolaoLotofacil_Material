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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.LastDrawCard
import com.cebolao.lotofacil.ui.components.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.SectionHeader
import com.cebolao.lotofacil.ui.components.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.components.StatisticsPanel
import com.cebolao.lotofacil.ui.components.WelcomeCard
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.syncMessageRes?.let { msgId ->
        val msg = stringResource(msgId)
        LaunchedEffect(msgId) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onMessageShown()
        }
    }

    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(
                onClick = viewModel::forceSync,
                enabled = !uiState.isSyncing
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimen.SmallIcon),
                        strokeWidth = Dimen.Border.Thick
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.home_sync_button_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Dimen.BottomBarSpacer)
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = Dimen.ScreenPadding,
                    vertical = Dimen.SmallPadding
                )
            ) {
                WelcomeCard()
            }

            val screenState = uiState.screenState
            
            Box(
                modifier = Modifier.padding(
                    horizontal = Dimen.ScreenPadding,
                    vertical = Dimen.MediumPadding
                )
            ) {
                when (screenState) {
                    is HomeScreenState.Success -> {
                        NextContestHeroCard(screenState.nextDrawInfo)
                    }
                    is HomeScreenState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimen.HeroCardMinHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> Unit
                }
            }

            if (screenState is HomeScreenState.Success) {
                screenState.lastDraw?.let { draw ->
                    SectionHeader(
                        stringResource(R.string.home_last_contest_format, draw.contestNumber)
                    )
                    LastDrawCard(
                        draw = draw,
                        winnerData = screenState.winnerData,
                        modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                    )
                }
            }

            SectionHeader(stringResource(R.string.home_statistics_center))
            
            uiState.statistics?.let { stats ->
                StatisticsPanel(
                    stats = stats,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding),
                    onTimeWindowSelected = viewModel::onTimeWindowSelected,
                    selectedWindow = uiState.selectedTimeWindow,
                    isStatsLoading = uiState.isStatsLoading
                )
                
                Spacer(Modifier.height(Dimen.MediumPadding))
                
                DistributionChartsCard(
                    stats = stats,
                    selectedPattern = uiState.selectedPattern,
                    onPatternSelected = viewModel::onPatternSelected,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
                
                Spacer(Modifier.height(Dimen.MediumPadding))
                
                StatisticsExplanationCard(
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
            } ?: run {
                if (uiState.isStatsLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.ScreenPadding)
                    )
                }
            }

            Spacer(Modifier.height(Dimen.SectionSpacing))
            
            InfoTipCard()
        }
    }
}

@Composable
private fun InfoTipCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimen.ScreenPadding),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
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
