package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
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
        viewModel.events.collectLatest { msgId -> snackbarHostState.showSnackbar(message = "Info: $msgId") } // Em produção, resolver stringRes
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
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimen.ScreenPadding)
        ) {
            Text(stringResource(R.string.checker_selection_instruction, LotofacilConstants.GAME_SIZE), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = Dimen.MediumPadding))
            SectionCard { NumberGrid(selectedNumbers = selectedNumbers, onNumberClick = viewModel::toggleNumber, maxSelection = LotofacilConstants.GAME_SIZE, modifier = Modifier.padding(Dimen.SmallPadding)) }
            Spacer(Modifier.height(Dimen.SectionSpacing))
            CheckerResultSection(uiState)
        }
    }
}

@Composable
private fun CheckerBottomBar(onSave: () -> Unit, onCheck: () -> Unit) {
    Surface(tonalElevation = Dimen.Elevation.Medium) {
        Row(Modifier.fillMaxWidth().padding(Dimen.MediumPadding), horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(Dimen.SmallPadding)); Text(stringResource(R.string.general_save)) }
            Button(onClick = onCheck, modifier = Modifier.weight(1f)) { Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(Dimen.SmallPadding)); Text(stringResource(R.string.checker_check_button)) }
        }
    }
}

@Composable
private fun CheckerResultSection(state: CheckerUiState) {
    when (state) {
        is CheckerUiState.Success -> {
            Text(stringResource(R.string.checker_performance_analysis), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimen.SmallPadding))
            CheckResultCard(state.result)
            Spacer(Modifier.height(Dimen.MediumPadding))
            SimpleStatsCard(state.simpleStats)
        }
        is CheckerUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
        is CheckerUiState.Error -> MessageState(Icons.Default.CheckCircle, stringResource(R.string.general_error_title), stringResource(state.messageResId), iconTint = MaterialTheme.colorScheme.error)
        CheckerUiState.Idle -> InfoListCard(stringResource(R.string.checker_how_it_works_title), stringResource(R.string.checker_how_it_works_desc), Icons.Default.CheckCircle)
    }
}