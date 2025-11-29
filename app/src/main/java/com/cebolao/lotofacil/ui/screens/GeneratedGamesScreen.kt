package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.navigateToChecker
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.GameAnalysisDialog
import com.cebolao.lotofacil.ui.components.GameCard
import com.cebolao.lotofacil.ui.components.GameCardAction
import com.cebolao.lotofacil.ui.components.LoadingDialog
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.MIME_TYPE_TEXT_PLAIN
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.GameAnalysisUiState
import com.cebolao.lotofacil.viewmodels.GameScreenEvent
import com.cebolao.lotofacil.viewmodels.GameSummary
import com.cebolao.lotofacil.viewmodels.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val TABS = listOf("Gerados", "Fixados")

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratedGamesScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val unpinned by viewModel.unpinnedGames.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedGames.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { TABS.size })
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is GameScreenEvent.ShareGame -> shareGameIntent(context, event.numbers)
            }
        }
    }

    // Handlers e Dialogs
    AnalysisDialogHandler(analysisState, viewModel::dismissAnalysisDialog, snackbarHostState)
    
    if (showClearDialog) {
        AppConfirmationDialog(
            title = R.string.games_clear_dialog_title,
            message = R.string.games_clear_dialog_message,
            confirmText = R.string.games_clear_confirm,
            onConfirm = { viewModel.clearUnpinned(); },
            onDismiss = { },
            icon = Icons.Default.DeleteSweep
        )
    }
    
    uiState.gameToDelete?.let {
        AppConfirmationDialog(
            title = R.string.games_delete_dialog_title,
            message = R.string.games_delete_dialog_message,
            confirmText = R.string.games_delete_confirm,
            onConfirm = viewModel::confirmDeleteGame,
            onDismiss = viewModel::dismissDeleteDialog,
            icon = Icons.Default.Delete
        )
    }

    AppScreen(
        title = stringResource(R.string.games_title),
        subtitle = stringResource(R.string.games_subtitle),
        navigationIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (unpinned.isNotEmpty()) {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.DeleteSweep, stringResource(R.string.games_clear_unpinned_button_description))
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (unpinned.isEmpty() && pinned.isEmpty()) {
                EmptyState(navController)
            } else {
                GameSummarySection(uiState.summary)
                
                // Tabs alinhadas
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    TABS.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                        )
                    }
                }
                
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    val games = if (page == 0) unpinned else pinned
                    
                    if (games.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if(page == 0) "Nenhum jogo gerado ainda" else "Nenhum jogo fixado",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        StandardPageLayout(contentPadding = PaddingValues(0.dp)) {
                            items(games, key = { it.numbers.hashCode() }) { game ->
                                AnimateOnEntry(modifier = Modifier.animateItemPlacement()) {
                                    GameCard(game, onAction = { action -> handleAction(action, game, viewModel, navController) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
private fun handleAction(action: GameCardAction, game: LotofacilGame, viewModel: GameViewModel, navController: NavController) {
    when (action) {
        GameCardAction.Analyze -> viewModel.analyzeGame(game)
        GameCardAction.Pin -> viewModel.togglePinState(game)
        GameCardAction.Delete -> viewModel.requestDeleteGame(game)
        GameCardAction.Check -> navController.navigateToChecker(game.numbers)
        GameCardAction.Share -> viewModel.shareGame(game)
    }
}

private fun shareGameIntent(context: android.content.Context, numbers: List<Int>) {
    val numbersStr = numbers.joinToString(", ") { "%02d".format(it) }
    val text = context.getString(R.string.share_game_message_template, numbersStr)
    val htmlText = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = MIME_TYPE_TEXT_PLAIN
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.games_share_game_subject))
        putExtra(Intent.EXTRA_TEXT, htmlText)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.games_share_chooser_title)))
}

@Composable
private fun AnalysisDialogHandler(state: GameAnalysisUiState, onDismiss: () -> Unit, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    when (state) {
        is GameAnalysisUiState.Success -> GameAnalysisDialog(state.result, onDismiss)
        is GameAnalysisUiState.Loading -> LoadingDialog(
            title = stringResource(R.string.games_analysis_dialog_title),
            message = stringResource(R.string.general_loading_analysis),
            onDismissRequest = {},
        )
        is GameAnalysisUiState.Error -> LaunchedEffect(state) {
            snackbarHostState.showSnackbar(context.getString(R.string.general_analysis_failed_snackbar))
            onDismiss()
        }
        else -> Unit
    }
}

@Composable
private fun EmptyState(navController: NavController) {
    MessageState(
        icon = Icons.AutoMirrored.Filled.ListAlt,
        title = stringResource(R.string.games_empty_state_title),
        message = stringResource(R.string.games_empty_state_description),
        actionLabel = stringResource(R.string.filters_button_generate),
        onActionClick = {
            navController.navigate(Screen.Filters.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
    )
}

@Composable
private fun GameSummarySection(summary: GameSummary) {
    val formatter = rememberCurrencyFormatter()
    SectionCard(modifier = Modifier.padding(start = Dimen.ScreenPadding, end = Dimen.ScreenPadding, top = Dimen.SmallPadding)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
            SummaryItem("Jogos", "${summary.totalGames}")
            SummaryItem("Fixados", "${summary.pinnedGames}", Icons.Default.PushPin)
            SummaryItem("Custo Total", formatter.format(summary.totalCost))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            icon?.let { Icon(it, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimen.SmallIcon)) }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}