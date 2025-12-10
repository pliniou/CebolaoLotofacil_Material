package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.CheckResultCard
import com.cebolao.lotofacil.ui.components.MessageState
import com.cebolao.lotofacil.ui.components.NumberGrid
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.SimpleStatsCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes
import com.cebolao.lotofacil.viewmodels.CheckerUiState
import com.cebolao.lotofacil.viewmodels.CheckerViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CheckerScreen(viewModel: CheckerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedNumbers by viewModel.selectedNumbers.collectAsStateWithLifecycle()
    val isGameComplete by viewModel.isGameComplete.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { msgId -> snackbarHostState.showSnackbar(message = "Info: $msgId") }
    }

    AppScreen(
        title = stringResource(R.string.checker_title),
        subtitle = stringResource(R.string.checker_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = viewModel::clearNumbers, enabled = selectedNumbers.isNotEmpty()) {
                Icon(Icons.Default.Delete, stringResource(R.string.checker_clear_button_description))
            }
        },
        bottomBar = { if (isGameComplete) CheckerBottomBar(viewModel::saveGame, viewModel::checkGame) }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.checker_selection_instruction, LotofacilConstants.GAME_SIZE),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "${selectedNumbers.size}/15",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (selectedNumbers.size == 15) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                NumberGrid(
                    selectedNumbers = selectedNumbers,
                    onNumberClick = viewModel::toggleNumber,
                    maxSelection = LotofacilConstants.GAME_SIZE,
                    sizeVariant = com.cebolao.lotofacil.ui.components.NumberBallSize.Large,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (uiState is CheckerUiState.Idle || uiState is CheckerUiState.Error) {
                 item {
                     SectionCard(modifier = Modifier.fillMaxWidth()) {
                         MessageState(
                            icon = Icons.Default.CheckCircle,
                            title = stringResource(R.string.checker_how_it_works_title),
                            message = stringResource(R.string.checker_how_it_works_desc),
                            modifier = Modifier.padding(Dimen.SpacingShort)
                        )
                     }
                 }
            }

            item {
                CheckerResultSection(uiState)
            }
        }
    }
}

@Composable
private fun CheckerBottomBar(onSave: () -> Unit, onCheck: () -> Unit) {
    Surface(
        tonalElevation = Dimen.Elevation.None,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.SpacingMedium)
                .padding(bottom = Dimen.SpacingMedium), // Extra bottom padding for navigation bar
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpacingMedium)
        ) {
            OutlinedButton(
                onClick = onSave, 
                modifier = Modifier.weight(1f).height(Dimen.ActionButtonHeight),
                shape = Shapes.medium
            ) { 
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(Dimen.SpacingShort))
                Text(stringResource(R.string.general_save)) 
            }
            Button(
                onClick = onCheck, 
                modifier = Modifier.weight(1f).height(Dimen.ActionButtonHeight),
                shape = Shapes.medium
            ) { 
                Icon(Icons.Default.CheckCircle, null) 
                Spacer(Modifier.width(Dimen.SpacingShort))
                Text(stringResource(R.string.checker_check_button)) 
            }
        }
    }
}

@Composable
private fun CheckerResultSection(state: CheckerUiState) {
    Column {
        when (state) {
            is CheckerUiState.Success -> {
                Text(stringResource(R.string.checker_performance_analysis), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Dimen.SpacingShort))
                CheckResultCard(state.result)
                Spacer(Modifier.height(Dimen.SpacingShort))
                SimpleStatsCard(state.simpleStats)
            }
            is CheckerUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
            is CheckerUiState.Error -> MessageState(Icons.Default.CheckCircle, stringResource(R.string.general_error_title), stringResource(state.messageResId), iconTint = MaterialTheme.colorScheme.error)
            CheckerUiState.Idle -> {}
        }
    }
}