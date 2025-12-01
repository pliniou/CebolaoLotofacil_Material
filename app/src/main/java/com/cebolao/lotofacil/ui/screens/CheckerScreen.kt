package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.BarChart
import com.cebolao.lotofacil.ui.components.CheckResultCard
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NumberBallSize
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.PrimaryActionButton
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.SimpleStatsCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.components.TitleWithIcon
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
    val isGameComplete by viewModel.isGameComplete.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

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
        navigationIcon = { Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CheckerBottomBar(
                selectedCount = selectedNumbers.size,
                isLoading = uiState is CheckerUiState.Loading,
                isGameComplete = isGameComplete,
                onClear = viewModel::clearNumbers,
                onCheck = viewModel::checkGame,
                onSave = viewModel::saveGame
            )
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
                    SelectionProgress(selectedNumbers.size)
                    SectionCard {
                        NumberGrid(
                            selectedNumbers = selectedNumbers,
                            onNumberClick = viewModel::toggleNumber,
                            maxSelection = LotofacilConstants.GAME_SIZE,
                            sizeVariant = NumberBallSize.Medium
                        )
                    }
                }
            }

            item {
                AnimatedContent(
                    targetState = uiState, 
                    label = "result",
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { state ->
                    when (state) {
                        is CheckerUiState.Success -> ResultSection(state.result, state.simpleStats)
                        is CheckerUiState.Error -> MessageState(
                            icon = Icons.Default.ErrorOutline,
                            title = stringResource(R.string.general_error_title),
                            message = stringResource(state.messageResId),
                            iconTint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = Dimen.LargePadding)
                        )
                        else -> Unit // Idle
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultSection(result: CheckResult, stats: ImmutableList<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing)) {
        AnimateOnEntry { CheckResultCard(result) }
        AnimateOnEntry(delayMillis = 100) { SimpleStatsCard(stats) }
        AnimateOnEntry(delayMillis = 200) { 
            SectionCard {
                TitleWithIcon(stringResource(R.string.checker_recent_hits_chart_title), iconVector = Icons.Default.Analytics)
                val chartData = remember(result) {
                    result.recentHits.map { it.first.toString().takeLast(4) to it.second }.toImmutableList()
                }
                BarChart(
                    data = chartData, 
                    maxValue = LotofacilConstants.GAME_SIZE, 
                    modifier = Modifier.fillMaxWidth().height(Dimen.ChartHeight)
                )
            }
        }
    }
}

@Composable
private fun SelectionProgress(count: Int) {
    val progress = count.toFloat() / LotofacilConstants.GAME_SIZE
    Column(
        Modifier.fillMaxWidth().padding(horizontal = Dimen.SmallPadding), 
        verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.checker_progress_format, count, LotofacilConstants.GAME_SIZE), style = MaterialTheme.typography.labelMedium)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(Dimen.ProgressBarHeight).clip(MaterialTheme.shapes.small),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CheckerBottomBar(
    selectedCount: Int,
    isLoading: Boolean,
    isGameComplete: Boolean,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onSave: () -> Unit
) {
    Surface(shadowElevation = Dimen.Elevation.High, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(Dimen.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                onClick = onClear,
                enabled = selectedCount > 0 && !isLoading,
                border = BorderStroke(Dimen.Border.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha=0.5f))
            ) { Icon(Icons.Default.Delete, stringResource(R.string.checker_clear_button_description)) }
            
            OutlinedIconButton(
                onClick = onSave,
                enabled = isGameComplete && !isLoading,
                border = BorderStroke(Dimen.Border.BorderThin, MaterialTheme.colorScheme.outline.copy(alpha=0.5f))
            ) { Icon(Icons.Default.Save, stringResource(R.string.checker_save_button_description)) }
            
            PrimaryActionButton(
                text = stringResource(R.string.checker_check_button),
                modifier = Modifier.weight(1f),
                enabled = isGameComplete,
                isLoading = isLoading,
                onClick = onCheck
            )
        }
    }
}