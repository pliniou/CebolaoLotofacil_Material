package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.ui.components.AppDivider
import com.cebolao.lotofacil.ui.components.DistributionChartsCard
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NextContestHeroCard
import com.cebolao.lotofacil.ui.components.NumberBallSize
import com.cebolao.lotofacil.ui.components.NumberBallVariant
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.components.StatisticsExplanationCard
import com.cebolao.lotofacil.ui.components.StatisticsPanel
import com.cebolao.lotofacil.ui.components.TitleWithIcon
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeUiState
import com.cebolao.lotofacil.viewmodels.HomeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Gestão de Efeitos (Snackbars)
    LaunchedEffect(uiState.syncMessageRes) {
        uiState.syncMessageRes?.let { resId ->
            snackbarHostState.showSnackbar(context.getString(resId))
            viewModel.onMessageShown()
        }
    }

    AppScreen(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_subtitle),
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_lotofacil_logo),
                contentDescription = null,
                modifier = Modifier.size(Dimen.LargeIcon)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (uiState.isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = viewModel::forceSync) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.home_sync_button_description))
                }
            }
        }
    ) { innerPadding ->
        HomeContent(
            uiState = uiState,
            onRetry = viewModel::retryInitialLoad,
            onTimeWindowSelected = viewModel::onTimeWindowSelected,
            onPatternSelected = viewModel::onPatternSelected,
            paddingValues = innerPadding
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onRetry: () -> Unit,
    onTimeWindowSelected: (Int) -> Unit,
    onPatternSelected: (StatisticPattern) -> Unit,
    paddingValues: PaddingValues
) {
    AnimatedContent(
        targetState = uiState.screenState, 
        label = "HomeContent",
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { state ->
        when (state) {
            is HomeScreenState.Loading -> MessageState(
                icon = Icons.Default.Refresh,
                title = stringResource(R.string.general_loading),
                message = stringResource(R.string.general_loading_analysis)
            )
            is HomeScreenState.Error -> MessageState(
                icon = Icons.Default.ErrorOutline,
                title = stringResource(R.string.general_failed_to_load_data),
                message = stringResource(state.messageResId),
                actionLabel = stringResource(R.string.general_retry),
                onActionClick = onRetry
            )
            is HomeScreenState.Success -> SuccessContent(
                state = state,
                uiState = uiState,
                onTimeWindowSelected = onTimeWindowSelected,
                onPatternSelected = onPatternSelected,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
private fun SuccessContent(
    state: HomeScreenState.Success,
    uiState: HomeUiState,
    onTimeWindowSelected: (Int) -> Unit,
    onPatternSelected: (StatisticPattern) -> Unit,
    paddingValues: PaddingValues
) {
    StandardPageLayout(scaffoldPadding = paddingValues) {
        // Hero Card: Próximo Concurso
        item { NextContestHeroCard(state.nextDrawInfo) }
        
        // Card Último Sorteio
        item { 
            LastDrawSection(state.lastDraw, state.winnerData.toImmutableList()) 
        }
        
        // Painel Estatístico
        uiState.statistics?.let { stats ->
            item { 
                StatisticsPanel(
                    stats = stats, 
                    isStatsLoading = uiState.isStatsLoading, 
                    selectedWindow = uiState.selectedTimeWindow, 
                    onTimeWindowSelected = onTimeWindowSelected
                ) 
            }
            item { 
                DistributionChartsCard(
                    stats = stats, 
                    selectedPattern = uiState.selectedPattern, 
                    onPatternSelected = onPatternSelected
                ) 
            }
        }
        
        // Card Educativo
        item { StatisticsExplanationCard() }
    }
}

@Composable
private fun LastDrawSection(
    lastDraw: HistoricalDraw?,
    winnerData: ImmutableList<WinnerData>
) {
    if (lastDraw == null) return
    SectionCard(
        header = {
            TitleWithIcon(
                text = stringResource(R.string.home_last_contest_format, lastDraw.contestNumber),
                iconVector = Icons.Default.History
            )
            lastDraw.date?.let { 
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) 
            }
        }
    ) {
        NumberGrid(
            selectedNumbers = lastDraw.numbers,
            onNumberClick = {},
            sizeVariant = NumberBallSize.Medium,
            ballVariant = NumberBallVariant.Secondary
        )
        
        AppDivider()
        
        WinnerInfoSection(winnerData)
    }
}

@Composable
private fun WinnerInfoSection(winnerData: ImmutableList<WinnerData>) {
    val currencyFormat = rememberCurrencyFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        TitleWithIcon(
            text = stringResource(R.string.home_winners_last_contest),
            iconVector = Icons.Default.EmojiEvents
        )
        
        winnerData.forEach { winnerInfo ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_hits_format, winnerInfo.hits),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(
                        if (winnerInfo.winnerCount == 1) R.string.home_winner_count_one 
                        else R.string.home_winner_count_other, 
                        winnerInfo.winnerCount
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    currencyFormat.format(winnerInfo.prize),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}