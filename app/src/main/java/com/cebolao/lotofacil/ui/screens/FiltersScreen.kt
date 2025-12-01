package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.FilterCard
import com.cebolao.lotofacil.ui.components.FilterStatsPanel
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(navController: NavController, viewModel: FiltersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Navegação e Efeitos
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToGeneratedGames -> {
                    navController.navigate(Screen.GeneratedGames.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is NavigationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(context.getString(event.messageRes))
                }
            }
        }
    }

    // Dialogs
    if (uiState.showResetDialog) {
        AppConfirmationDialog(
            title = R.string.filters_reset_dialog_title,
            message = R.string.filters_reset_dialog_message,
            confirmText = R.string.filters_reset_confirm,
            onConfirm = viewModel::confirmResetFilters,
            onDismiss = viewModel::dismissResetDialog,
            icon = Icons.Default.Refresh
        )
    }

    uiState.filterInfoToShow?.let { filterType ->
        InfoDialog(
            dialogTitle = stringResource(R.string.filters_info_dialog_title_format, stringResource(filterType.titleRes)),
            icon = Icons.Default.Tune,
            onDismissRequest = viewModel::dismissFilterInfo
        ) { FormattedText(stringResource(filterType.descriptionRes)) }
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        navigationIcon = { Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = viewModel::requestResetFilters) {
                Icon(Icons.Default.Refresh, stringResource(R.string.filters_reset_button_description))
            }
        },
        bottomBar = {
            GenerationActionsPanel(
                generationState = uiState.generationState,
                onGenerate = viewModel::generateGames,
                onCancel = viewModel::cancelGeneration
            )
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            
            // Stats Panel
            item {
                AnimateOnEntry {
                    FilterStatsPanel(
                        activeFilters = uiState.filterStates.filter { it.isEnabled },
                        successProbability = uiState.successProbability
                    )
                }
            }

            item { Spacer(Modifier.height(Dimen.SmallPadding)) }

            // Lista de Filtros
            uiState.filterStates.forEachIndexed { index, filter ->
                item(key = filter.type.name) {
                    AnimateOnEntry(delayMillis = (index * 30).toLong()) { // Delay reduzido para UI mais ágil
                        FilterCard(
                            filterState = filter,
                            onEnabledChange = { viewModel.onFilterToggle(filter.type, it) },
                            onRangeChange = { range -> viewModel.onRangeAdjust(filter.type, range) },
                            onInfoClick = { viewModel.showFilterInfo(filter.type) },
                            lastDrawNumbers = uiState.lastDraw
                        )
                    }
                }
            }
            
            // Spacer final para compensar BottomBar
            item { Spacer(Modifier.height(Dimen.BottomContentPadding)) }
        }
    }
}