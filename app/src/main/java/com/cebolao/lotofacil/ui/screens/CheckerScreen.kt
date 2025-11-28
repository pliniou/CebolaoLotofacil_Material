package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.CheckerUiEvent
import com.cebolao.lotofacil.viewmodels.CheckerUiState
import com.cebolao.lotofacil.viewmodels.CheckerViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CheckerScreen(viewModel: CheckerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedNumbers by viewModel.selectedNumbers.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    val isButtonEnabled by remember(selectedNumbers, uiState) {
        derivedStateOf { selectedNumbers.size == LotofacilConstants.GAME_SIZE && uiState !is CheckerUiState.Loading }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CheckerUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(context.getString(event.messageResId))
            }
        }
    }

    AppScreen(
        title = stringResource(R.string.checker_title),
        subtitle = stringResource(R.string.checker_subtitle),
        navigationIcon = { Icon(Icons.Default.Analytics, stringResource(R.string.checker_title), tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionsBar(
                selectedCount = selectedNumbers.size,
                isLoading = uiState is CheckerUiState.Loading,
                isButtonEnabled = isButtonEnabled,
                onClear = viewModel::clearNumbers,
                onCheck = viewModel::checkGame,
                onSave = viewModel::saveGame
            )
        }
    ) { innerPadding ->
        CheckerContent(uiState, selectedNumbers, viewModel::toggleNumber, innerPadding)
    }
}

@Composable
private fun CheckerContent(
    uiState: CheckerUiState,
    selectedNumbers: Set<Int>,
    onToggleNumber: (Int) -> Unit,
    paddingValues: PaddingValues
) {
    // Recursividade: StandardPageLayout
    StandardPageLayout(contentPadding = paddingValues) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardSpacing)) {
                SelectionProgress(selectedNumbers.size)
                AnimateOnEntry {
                    SectionCard {
                        NumberGrid(
                            selectedNumbers = selectedNumbers,
                            onNumberClick = onToggleNumber,
                            maxSelection = LotofacilConstants.GAME_SIZE,
                            numberSize = Dimen.NumberBallSmall
                        )
                    }
                }
            }
        }

        item {
            AnimatedContent(targetState = uiState, label = "result") { state ->
                when (state) {
                    is CheckerUiState.Success -> ResultSection(state.result, state.simpleStats)
                    is CheckerUiState.Error -> MessageState(
                        icon = Icons.Default.ErrorOutline,
                        title = stringResource(R.string.general_error_title),
                        message = stringResource(state.messageResId),
                        iconTint = MaterialTheme.colorScheme.error
                    )
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun ResultSection(result: CheckResult, stats: ImmutableList<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardSpacing)) {
        AnimateOnEntry { CheckResultCard(result) }
        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_CHECKER_RESULT) { SimpleStatsCard(stats) }
        AnimateOnEntry(delayMillis = AppConfig.Animation.DELAY_CHECKER_RESULT * 2L) { BarChartCard(result) }
    }
}

@Composable
private fun BarChartCard(result: CheckResult) {
    SectionCard {
        TitleWithIcon(stringResource(R.string.checker_recent_hits_chart_title), Icons.Default.Analytics)
        val chartData = result.recentHits.map { it.first.toString().takeLast(AppConfig.UI.CHECKER_CHART_SUFFIX_LENGTH) to it.second }
        val maxValue = (chartData.maxOfOrNull { it.second }?.coerceAtLeast(AppConfig.UI.CHECKER_CHART_MIN_MAX_VALUE) ?: AppConfig.UI.CHECKER_CHART_MIN_MAX_VALUE)
        BarChart(data = chartData.toImmutableList(), maxValue = maxValue, modifier = Modifier.fillMaxWidth().height(Dimen.BarChartHeight))
    }
}

@Composable
private fun SelectionProgress(count: Int) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        LinearProgressIndicator(progress = { count.toFloat() / LotofacilConstants.GAME_SIZE }, modifier = Modifier.fillMaxWidth().height(Dimen.ProgressBarHeight).clip(MaterialTheme.shapes.small))
        Text(stringResource(R.string.checker_progress_format, count, LotofacilConstants.GAME_SIZE), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BottomActionsBar(
    selectedCount: Int,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onSave: () -> Unit
) {
    Surface(shadowElevation = Dimen.Elevation.Level4, tonalElevation = Dimen.Elevation.Level2) {
        Row(
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = Dimen.CardPadding, vertical = Dimen.MediumPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(onClick = onClear, enabled = selectedCount > 0 && !isLoading) {
                Icon(Icons.Default.Delete, stringResource(R.string.checker_clear_button_description))
            }
            OutlinedIconButton(onClick = onSave, enabled = isButtonEnabled) {
                Icon(Icons.Default.Save, stringResource(R.string.checker_save_button_description))
            }
            PrimaryActionButton(modifier = Modifier.weight(1f).height(Dimen.LargeButtonHeight), enabled = isButtonEnabled, loading = isLoading, onClick = onCheck) {
                Text(stringResource(R.string.checker_check_button))
            }
        }
    }
}