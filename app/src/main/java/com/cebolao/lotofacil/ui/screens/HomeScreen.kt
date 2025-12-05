package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    uiState.syncMessageRes?.let { msgId ->
        val msg = stringResource(msgId)
        LaunchedEffect(msgId) { snackbarHostState.showSnackbar(msg); viewModel.onMessageShown() }
    }

    AppScreen(
        title = stringResource(R.string.app_name),
        subtitle = stringResource(R.string.home_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = viewModel::forceSync, enabled = !uiState.isSyncing) {
                if (uiState.isSyncing) CircularProgressIndicator(Modifier.size(Dimen.SmallIcon), strokeWidth = Dimen.Border.Thick)
                else Icon(Icons.Default.Refresh, stringResource(R.string.home_sync_button_description))
            }
        }
    ) { inner ->
        Column(Modifier.padding(inner).fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Dimen.BottomBarSpacer)) {
            Box(Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)) { WelcomeCard() }

            val screenState = uiState.screenState
            
            Box(Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding)) {
                when (screenState) {
                    is HomeScreenState.Success -> NextContestHeroCard(screenState.nextDrawInfo)
                    is HomeScreenState.Loading -> Box(Modifier.fillMaxWidth().height(Dimen.HeroCardMinHeight), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    else -> Unit
                }
            }

            if (screenState is HomeScreenState.Success) {
                screenState.lastDraw?.let { draw ->
                    SectionHeader(stringResource(R.string.home_last_contest_format, draw.contestNumber))
                    LastDrawCard(draw, screenState.winnerData, Modifier.padding(horizontal = Dimen.ScreenPadding))
                }
            }

            SectionHeader(stringResource(R.string.home_statistics_center))
            uiState.statistics?.let { stats ->
                StatisticsPanel(stats, Modifier.padding(horizontal = Dimen.ScreenPadding), viewModel::onTimeWindowSelected, uiState.selectedTimeWindow, uiState.isStatsLoading)
                Spacer(Modifier.height(Dimen.MediumPadding))
                DistributionChartsCard(stats, uiState.selectedPattern, viewModel::onPatternSelected, Modifier.padding(horizontal = Dimen.ScreenPadding))
                Spacer(Modifier.height(Dimen.MediumPadding))
                StatisticsExplanationCard(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding))
            } ?: if (uiState.isStatsLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(Dimen.ScreenPadding)) else Unit

            Spacer(Modifier.height(Dimen.SectionSpacing))
            InfoTipCard()
        }
    }
}

@Composable private fun SectionHeader(title: String) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = Dimen.ScreenPadding, top = Dimen.LargePadding, bottom = Dimen.SmallPadding)) }
@Composable private fun InfoTipCard() { OutlinedCard(Modifier.fillMaxWidth().padding(horizontal = Dimen.ScreenPadding), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) { Row(Modifier.padding(Dimen.MediumPadding), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(Dimen.MediumPadding)); Text(stringResource(R.string.general_disclaimer_responsibility), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
