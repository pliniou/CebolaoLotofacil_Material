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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.CheckerUiState
import com.cebolao.lotofacil.viewmodels.CheckerViewModel

@Composable
fun CheckerScreen(viewModel: CheckerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedNumbers by viewModel.selectedNumbers.collectAsStateWithLifecycle()
    val isGameComplete by viewModel.isGameComplete.collectAsStateWithLifecycle()
    
    AppScreen(
        title = stringResource(R.string.checker_title),
        subtitle = stringResource(R.string.checker_subtitle),
        actions = {
             IconButton(onClick = viewModel::clearNumbers, enabled = selectedNumbers.isNotEmpty()) {
                 Icon(Icons.Default.Delete, stringResource(R.string.checker_clear_selection))
             }
        },
        bottomBar = {
            if (isGameComplete) {
                Surface(tonalElevation = 4.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::saveGame, 
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.general_save))
                        }
                        Button(
                            onClick = viewModel::checkGame, 
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.checker_check_button))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimen.ScreenPadding)
        ) {
            // 1. Área de Seleção (Mesa)
            Text(
                stringResource(R.string.checker_selection_instruction, LotofacilConstants.GAME_SIZE),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            SectionCard {
                NumberGrid(
                    selectedNumbers = selectedNumbers,
                    onNumberClick = viewModel::toggleNumber,
                    maxSelection = LotofacilConstants.GAME_SIZE,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // 2. Resultados (Dinâmico)
            Spacer(Modifier.height(Dimen.SectionSpacing))
            
            when (val state = uiState) {
                is CheckerUiState.Success -> {
                    Text(stringResource(R.string.checker_performance_analysis), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CheckResultCard(state.result)
                    Spacer(Modifier.height(16.dp))
                    SimpleStatsCard(state.simpleStats)
                }
                is CheckerUiState.Loading -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                is CheckerUiState.Error -> {
                    Text(stringResource(state.messageResId), color = MaterialTheme.colorScheme.error)
                }
                else -> { /* Idle - Info Tip */
                    InfoListCard(
                        title = stringResource(R.string.checker_how_it_works_title),
                        subtitle = stringResource(R.string.checker_how_it_works_desc),
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
            
            Spacer(Modifier.height(Dimen.BottomBarSpacer))
        }
    }
}