package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.HomeScreenState
import com.cebolao.lotofacil.viewmodels.HomeUiState
import com.cebolao.lotofacil.viewmodels.HomeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()

    // Consolidação de Efeitos
    HomeScreenEffects(
        uiState = uiState,
        pullRefreshState = pullToRefreshState,
        snackbarHost = snackbarHostState,
        viewModel = viewModel
    )

    AppScreen(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_subtitle),
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_lotofacil_logo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimen.LargeIcon)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (uiState.isSyncing && uiState.screenState !is HomeScreenState.Loading && !pullToRefreshState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(Dimen.MediumIcon).padding(end = Dimen.SmallPadding))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            HomeContent(uiState, viewModel)
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenEffects(
    uiState: HomeUiState,
    pullRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
    snackbarHost: SnackbarHostState,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) { viewModel.forceSync() }
    }

    LaunchedEffect(uiState.isSyncing) {
        if (!uiState.isSyncing) pullRefreshState.endRefresh()
    }

    LaunchedEffect(uiState.showSyncFailedMessage, uiState.showSyncSuccessMessage) {
        if (uiState.showSyncFailedMessage) {
            snackbarHost.showSnackbar(context.getString(R.string.home_sync_failed_message))
            viewModel.onSyncMessageShown()
        }
        if (uiState.showSyncSuccessMessage) {
            snackbarHost.showSnackbar(context.getString(R.string.home_sync_success_message))
            viewModel.onSyncSuccessMessageShown()
        }
    }
}

@Composable
private fun HomeContent(uiState: HomeUiState, viewModel: HomeViewModel) {
    AnimatedContent(targetState = uiState.screenState, label = "HomeContent") { state ->
        when (state) {
            is HomeScreenState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is HomeScreenState.Error -> {
                MessageState(
                    icon = Icons.Default.ErrorOutline,
                    title = stringResource(R.string.general_failed_to_load_data),
                    message = stringResource(state.messageResId),
                    actionLabel = stringResource(R.string.general_retry),
                    onActionClick = viewModel::retryInitialLoad,
                    iconTint = MaterialTheme.colorScheme.error
                )
            }
            is HomeScreenState.Success -> {
                // Recursividade: Usa StandardPageLayout
                StandardPageLayout {
                    item(key = "next_draw") {
                        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_NEXT_DRAW) {
                            state.nextDrawInfo?.let { NextContestCard(it) }
                        }
                    }
                    item(key = "last_draw") {
                        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_LAST_DRAW) {
                            state.lastDraw?.let { 
                                LastDrawSection(
                                    lastDraw = it,
                                    winnerData = state.winnerData.toImmutableList(),
                                    simpleStats = state.lastDrawSimpleStats,
                                    checkResult = state.lastDrawCheckResult
                                )
                            }
                        }
                    }
                    item(key = "statistics") {
                        uiState.statistics?.let {
                            AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_STATS) {
                                StatisticsPanel(
                                    stats = it,
                                    isStatsLoading = uiState.isStatsLoading,
                                    selectedWindow = uiState.selectedTimeWindow,
                                    onTimeWindowSelected = viewModel::onTimeWindowSelected
                                )
                            }
                        }
                    }
                    item(key = "charts") {
                        uiState.statistics?.let {
                            AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_CHARTS) {
                                DistributionChartsCard(
                                    stats = it,
                                    selectedPattern = uiState.selectedPattern,
                                    onPatternSelected = viewModel::onPatternSelected
                                )
                            }
                        }
                    }
                    item(key = "explanation") {
                        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_EXPLANATION) {
                            StatisticsExplanationCard()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NextContestCard(info: NextDrawInfo) {
    SectionCard {
        TitleWithIcon(
            text = stringResource(R.string.home_next_contest, info.contestNumber),
            icon = Icons.Default.CalendarToday
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
        ) {
            Text(info.formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimen.SmallPadding))
            Text(stringResource(R.string.home_prize_estimate), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = info.formattedPrize,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            AppDivider(Modifier.padding(vertical = Dimen.SmallPadding))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(Dimen.SmallIcon))
                Text(stringResource(R.string.home_accumulated_prize_final_five), style = MaterialTheme.typography.bodySmall)
                Text(info.formattedPrizeFinalFive, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun LastDrawSection(
    lastDraw: HistoricalDraw,
    winnerData: ImmutableList<WinnerData>,
    simpleStats: ImmutableList<Pair<String, String>>,
    checkResult: CheckResult?
) {
    val statsPagerState = rememberPagerState(pageCount = { 2 })

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recursividade: TitleWithIcon
            TitleWithIcon(
                text = stringResource(R.string.home_last_contest_format, lastDraw.contestNumber),
                icon = Icons.Default.History
            )
            lastDraw.date?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.BallSpacing, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(Dimen.BallSpacing),
            maxItemsInEachRow = AppConfig.UI.GRID_COLUMNS
        ) {
            lastDraw.numbers.sorted().forEach {
                NumberBall(it, size = Dimen.NumberBall, variant = NumberBallVariant.Lotofacil)
            }
        }

        AppDivider()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(state = statsPagerState) { page ->
                when (page) {
                    0 -> SimpleStatsCard(stats = simpleStats, modifier = Modifier.padding(Dimen.SmallPadding))
                    1 -> checkResult?.let { BarChartCard(result = it, modifier = Modifier.padding(Dimen.SmallPadding)) }
                }
            }
            Spacer(Modifier.height(Dimen.SmallPadding))
            PagerIndicator(pageCount = 2, currentPage = statsPagerState.currentPage)
        }

        if (winnerData.isNotEmpty()) {
            AppDivider()
            WinnerInfoSection(winnerData)
        }
    }
}

// WinnerInfoSection e BarChartCard mantidos (apenas pequenos ajustes de imports)
@Composable
private fun WinnerInfoSection(winnerData: ImmutableList<WinnerData>) {
    val currencyFormat = rememberCurrencyFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Text(stringResource(R.string.home_winners_last_contest), style = MaterialTheme.typography.titleMedium)
        winnerData.forEach { winnerInfo ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_hits_format, winnerInfo.hits), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(stringResource(if (winnerInfo.winnerCount == 1) R.string.home_winner_count_one else R.string.home_winner_count_other, winnerInfo.winnerCount), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Text(currencyFormat.format(winnerInfo.prize), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun BarChartCard(result: CheckResult, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Text(stringResource(R.string.checker_recent_hits_chart_title), style = MaterialTheme.typography.titleMedium)
        val chartData = result.recentHits.map { it.first.toString().takeLast(AppConfig.UI.CHECKER_CHART_SUFFIX_LENGTH) to it.second }
        val maxValue = (chartData.maxOfOrNull { it.second }?.coerceAtLeast(AppConfig.UI.CHECKER_CHART_MIN_MAX_VALUE) ?: AppConfig.UI.CHECKER_CHART_MIN_MAX_VALUE)
        BarChart(data = chartData.toImmutableList(), maxValue = maxValue, modifier = Modifier.fillMaxWidth().height(Dimen.BarChartHeight))
    }
}