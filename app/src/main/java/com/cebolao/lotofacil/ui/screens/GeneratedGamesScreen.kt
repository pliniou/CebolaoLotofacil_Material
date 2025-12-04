package com.cebolao.lotofacil.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.FontFamilyNumeric
import com.cebolao.lotofacil.util.MIME_TYPE_TEXT_PLAIN
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GeneratedGamesScreen(navController: NavController, viewModel: GameViewModel = hiltViewModel()) {
    val unpinned by viewModel.unpinnedGames.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedGames.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameScreenEvent.ShareGame -> context.shareGameIntent(event.numbers)
            }
        }
    }

    AnalysisDialogHandler(analysisState, viewModel::dismissAnalysisDialog, snackbarHostState)
    ConfirmationsHandler(
        showClearDialog = showClearDialog,
        gameToDelete = uiState.gameToDelete,
        onClearConfirm = { viewModel.clearUnpinned(); showClearDialog = false },
        onClearDismiss = { showClearDialog = false },
        onDeleteConfirm = viewModel::confirmDeleteGame,
        onDeleteDismiss = viewModel::dismissDeleteDialog
    )

    AppScreen(
        title = stringResource(R.string.games_title),
        subtitle = stringResource(R.string.games_subtitle),
        navigationIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (unpinned.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, stringResource(R.string.games_clear_unpinned_button_description))
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            if (unpinned.isNotEmpty() || pinned.isNotEmpty()) {
                GameSummarySection(uiState.summary)
            }
            GameTabs(pagerState.currentPage) { index -> scope.launch { pagerState.animateScrollToPage(index) } }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val games = if (page == 0) unpinned else pinned
                GameList(
                    games = games,
                    isNewGamesTab = (page == 0),
                    onGenerateRequest = {
                        navController.navigate(Screen.Filters.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAction = { action, game -> 
                        when (action) {
                            GameCardAction.Analyze -> viewModel.analyzeGame(game)
                            GameCardAction.Pin -> viewModel.togglePinState(game)
                            GameCardAction.Delete -> viewModel.requestDeleteGame(game)
                            GameCardAction.Check -> navController.navigateToChecker(game.numbers)
                            GameCardAction.Share -> viewModel.shareGame(game)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GameList(
    games: List<LotofacilGame>,
    isNewGamesTab: Boolean,
    onGenerateRequest: () -> Unit,
    onAction: (GameCardAction, LotofacilGame) -> Unit
) {
    if (games.isEmpty()) {
        EmptyState(isNewGamesTab, onGenerateRequest)
    } else {
        StandardPageLayout(scaffoldPadding = PaddingValues()) {
            items(
                items = games,
                key = { it.numbers.hashCode() },
                contentType = { "game_card" }
            ) { game ->
                AnimateOnEntry {
                    GameCard(game) { action -> onAction(action, game) }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationsHandler(
    showClearDialog: Boolean,
    gameToDelete: LotofacilGame?,
    onClearConfirm: () -> Unit,
    onClearDismiss: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit
) {
    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = onClearConfirm,
            onDismiss = onClearDismiss,
            icon = Icons.Default.DeleteSweep
        )
    }
    if (gameToDelete != null) {
        AppConfirmationDialog(
            title = R.string.games_delete_dialog_title,
            message = R.string.games_delete_dialog_message,
            confirmText = R.string.games_delete_confirm,
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
            icon = Icons.Default.Delete
        )
    }
}

@Composable
private fun EmptyState(isNewGamesTab: Boolean, onGenerateRequest: () -> Unit) {
    MessageState(
        icon = Icons.AutoMirrored.Filled.ListAlt,
        title = stringResource(R.string.games_empty_state_title),
        message = stringResource(if(isNewGamesTab) R.string.games_empty_state_description else R.string.widget_no_pinned_games),
        actionLabel = if(isNewGamesTab) stringResource(R.string.filters_button_generate) else null,
        onActionClick = if(isNewGamesTab) onGenerateRequest else null,
        modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTabs(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    SecondaryTabRow(selectedTabIndex = selectedIndex, containerColor = MaterialTheme.colorScheme.background, divider = {}) {
        listOf(R.string.games_tab_new, R.string.games_tab_pinned).forEachIndexed { index, titleRes ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium, fontWeight = if(selectedIndex == index) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
private fun GameSummarySection(summary: GameSummary) {
    val formatter = rememberCurrencyFormatter()
    SectionCard(modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.MediumPadding)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
            SummaryItem("Gerados", "${summary.totalGames}")
            SummaryItem("Investimento", formatter.format(summary.totalCost))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.displaySmall, fontFamily = FontFamilyNumeric, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AnalysisDialogHandler(state: GameAnalysisUiState, onDismiss: () -> Unit, snackbarHostState: SnackbarHostState) {
    when (state) {
        is GameAnalysisUiState.Success -> GameAnalysisDialog(state.result, onDismiss)
        is GameAnalysisUiState.Loading -> LoadingDialog(stringResource(R.string.games_analysis_dialog_title), stringResource(R.string.general_loading_analysis), {})
        is GameAnalysisUiState.Error -> {
            val message = stringResource(R.string.general_analysis_failed_snackbar)
            LaunchedEffect(state) {
                snackbarHostState.showSnackbar(message)
                onDismiss()
            }
        }
        else -> Unit
    }
}

private fun Context.shareGameIntent(numbers: List<Int>) {
    val numbersStr = numbers.joinToString(", ") { "%02d".format(it) }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = MIME_TYPE_TEXT_PLAIN
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.games_share_game_subject))
        putExtra(Intent.EXTRA_TEXT, HtmlCompat.fromHtml(getString(R.string.share_game_message_template, numbersStr), HtmlCompat.FROM_HTML_MODE_COMPACT).toString())
    }
    startActivity(Intent.createChooser(intent, getString(R.string.games_share_chooser_title)))
}