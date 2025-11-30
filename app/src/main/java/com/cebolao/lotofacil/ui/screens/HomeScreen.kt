package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
    val context = androidx.compose.ui.platform.LocalContext.current

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
                StandardPageLayout {
                    item(key = "next_draw") {
                        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_NEXT_DRAW) {
                            state.nextDrawInfo?.let { NextContestHeroCard(it) }
                        }
                    }
                    item(key = "last_draw") {
                        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_LAST_DRAW) {
                            state.lastDraw?.let { 
                                LastDrawSection(
                                    lastDraw = it,
                                    winnerData = state.winnerData.toImmutableList(),
                                    simpleStats = state.lastDrawSimpleStats,
                                    lastDrawCheckResult = state.lastDrawCheckResult
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
private fun NextContestHeroCard(info: NextDrawInfo) {
    // Gradiente sutil usando Primary Container
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.6f),
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(Dimen.Elevation.Low),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Column(
                modifier = Modifier
                    .padding(Dimen.LargePadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimen.MediumIcon)
                    )
                    Spacer(Modifier.width(Dimen.SmallPadding))
                    Text(
                        stringResource(R.string.home_next_contest, info.contestNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = info.formattedPrize,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = info.formattedDate,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = Dimen.SmallPadding), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(Dimen.SmallIcon))
                    Spacer(Modifier.width(Dimen.ExtraSmallPadding))
                    Text(
                        stringResource(R.string.home_accumulated_prize_final_five),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(Dimen.SmallPadding))
                    Text(
                        info.formattedPrizeFinalFive,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
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
    lastDrawCheckResult: CheckResult?
) {
    val statsPagerState = rememberPagerState(pageCount = { 2 })

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    0 -> SimpleStatsCard(stats = simpleStats, modifier = Modifier.padding(Dimen.ExtraSmallPadding))
                    1 -> lastDrawCheckResult?.let { BarChartCard(result = it, modifier = Modifier.padding(Dimen.ExtraSmallPadding)) }
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

@Composable
private fun WinnerInfoSection(winnerData: ImmutableList<WinnerData>) {
    val currencyFormat = rememberCurrencyFormatter()
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        Text(stringResource(R.string.home_winners_last_contest), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        winnerData.forEach { winnerInfo ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_hits_format, winnerInfo.hits), 
                    modifier = Modifier.weight(0.8f), 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(if (winnerInfo.winnerCount == 1) R.string.home_winner_count_one else R.string.home_winner_count_other, winnerInfo.winnerCount), 
                    modifier = Modifier.weight(1.2f), 
                    style = MaterialTheme.typography.bodySmall, 
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    currencyFormat.format(winnerInfo.prize), 
                    modifier = Modifier.weight(1.5f), 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, 
                    textAlign = TextAlign.End
                )
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