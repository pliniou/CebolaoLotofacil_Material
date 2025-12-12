package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.LastDrawCard
import com.cebolao.lotofacil.ui.components.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.SectionHeader
import com.cebolao.lotofacil.ui.components.StandardPageLayout
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
                        modifier = Modifier.size(Dimen.IconSmall),
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
        // StandardPageLayout handles horizontal padding now (ScreenPadding)
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item {
                AnimateOnEntry(delayMillis = 0) {
                    WelcomeCard()
                }
            }

            item {
                val screenState = uiState.screenState
                AnimateOnEntry(delayMillis = 100) {
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
            }

            if (uiState.screenState is HomeScreenState.Success) {
                val draw = (uiState.screenState as HomeScreenState.Success).lastDraw
                if (draw != null) {
                    item {
                        AnimateOnEntry(delayMillis = 200) {
                            Column(
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                                    Dimen.SpacingShort
                                )
                            ) {
                                SectionHeader(
                                    stringResource(
                                        R.string.home_last_contest_format,
                                        draw.contestNumber
                                    )
                                )
                                LastDrawCard(
                                    draw = draw,
                                    winnerData = (uiState.screenState as HomeScreenState.Success).winnerData,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            uiState.statistics?.let { stats ->
                item {
                    AnimateOnEntry(delayMillis = 300) {
                        StatisticsPanel(
                            stats = stats,
                            modifier = Modifier.fillMaxWidth(),
                            onTimeWindowSelected = viewModel::onTimeWindowSelected,
                            selectedWindow = uiState.selectedTimeWindow,
                            isStatsLoading = uiState.isStatsLoading
                        )
                    }
                }

                item {
                    AnimateOnEntry(delayMillis = 400) {
                        DistributionChartsCard(
                            stats = stats,
                            selectedPattern = uiState.selectedPattern,
                            onPatternSelected = viewModel::onPatternSelected,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    AnimateOnEntry(delayMillis = 500) {
                        StatisticsExplanationCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                AnimateOnEntry(delayMillis = 600) {
                    InfoTipCard()
                }
            }
        }
    }
}

@Composable
private fun InfoTipCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
        ),
        // Flat style: No border or subtle border
        border = androidx.compose.foundation.BorderStroke(
            Dimen.Border.Hairline,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimen.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.width(Dimen.SpacingMedium))

            Text(
                text = stringResource(R.string.general_disclaimer_responsibility),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
