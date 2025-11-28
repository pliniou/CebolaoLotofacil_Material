package com.cebolao.lotofacil.ui.screens

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
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.MIME_TYPE_TEXT_PLAIN
import com.cebolao.lotofacil.util.rememberCurrencyFormatter
import com.cebolao.lotofacil.viewmodels.*
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

    // Dialogs Recursivos
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
                
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    TABS.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title) }
                        )
                    }
                }
                
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    // Uso do StandardPageLayout interno ao Pager
                    StandardPageLayout(contentPadding = PaddingValues(0.dp)) { // Padding 0 pois StandardPageLayout já aplica o default
                        val games = if (page == 0) unpinned else pinned
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

// Helpers mantidos iguais ao anterior, pois são específicos desta tela
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
    SectionCard(modifier = Modifier.padding(Dimen.ScreenPadding)) {
        TitleWithIcon(text = "Resumo", icon = Icons.Default.Style)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
            SummaryItem("Total", "${summary.totalGames}")
            SummaryItem("Fixados", "${summary.pinnedGames}", Icons.Default.PushPin)
            SummaryItem("Custo", formatter.format(summary.totalCost))
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: ImageVector? = null) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)) {
            icon?.let { Icon(it, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimen.SmallIcon)) }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}