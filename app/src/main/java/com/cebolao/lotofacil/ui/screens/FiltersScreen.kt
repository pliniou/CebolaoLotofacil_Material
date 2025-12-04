package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.filterIcon
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(navController: NavController, viewModel: FiltersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is NavigationEvent.NavigateToGeneratedGames) {
                navController.navigate(Screen.GeneratedGames.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    if (uiState.showResetDialog) {
        AppConfirmationDialog(R.string.filters_reset_dialog_title, R.string.filters_reset_dialog_message, R.string.filters_reset_confirm, viewModel::confirmResetFilters, viewModel::dismissResetDialog, icon = Icons.Default.DeleteSweep)
    }

    uiState.filterInfoToShow?.let { type ->
        InfoDialog(stringResource(R.string.filters_info_dialog_title_format, stringResource(type.titleRes)), type.filterIcon, viewModel::dismissFilterInfo) {
            SectionCard { InfoPoint("Definição", stringResource(R.string.filters_info_button_description)) } // Placeholder desc
        }
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        actions = { TextButton(onClick = viewModel::requestResetFilters) { Text(stringResource(R.string.filters_reset_button_description)) } },
        bottomBar = { GenerationActionsPanel(uiState.generationState, viewModel::generateGames, viewModel::cancelGeneration) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentPadding = PaddingValues(bottom = Dimen.BottomBarSpacer)) {
            item { FilterPresetSelector(viewModel::applyPreset, Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)) }
            item { FilterStatsPanel(uiState.filterStates.filter { it.isEnabled }, uiState.successProbability, Modifier.padding(horizontal = Dimen.ScreenPadding)) }
            item { HorizontalDivider(Modifier.padding(vertical = Dimen.SmallPadding), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            
            items(uiState.filterStates, key = { it.type.name }) { filter ->
                FilterCard(filter, { viewModel.onFilterToggle(filter.type, it) }, { viewModel.onRangeAdjust(filter.type, it) }, { viewModel.showFilterInfo(filter.type) }, uiState.lastDraw, Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.ExtraSmallPadding))
            }
        }
    }
}