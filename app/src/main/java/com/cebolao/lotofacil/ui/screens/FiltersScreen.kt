package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.FilterCard
import com.cebolao.lotofacil.ui.components.FilterPresetSelector
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.InfoPoint
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.filterIcon
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(navController: NavController, viewModel: FiltersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                    val message = if (event.labelRes != null) {
                        context.getString(event.messageRes, context.getString(event.labelRes))
                    } else {
                        context.getString(event.messageRes)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    if (uiState.showResetDialog) {
        AppConfirmationDialog(R.string.filters_reset_dialog_title, R.string.filters_reset_dialog_message, R.string.filters_reset_confirm, viewModel::confirmResetFilters, viewModel::dismissResetDialog, icon = Icons.Default.DeleteSweep)
    }

    uiState.filterInfoToShow?.let { type ->
        InfoDialog(stringResource(R.string.filters_info_dialog_title_format, stringResource(type.titleRes)), type.filterIcon, viewModel::dismissFilterInfo) {
            SectionCard {
                InfoPoint(
                    stringResource(R.string.filters_info_button_description),
                    stringResource(type.descriptionRes)
                )
            }
        }
    }

    val allFilters: List<FilterState> = uiState.filterStates
    val partitionResult = allFilters.partition { 
         it.type in listOf(
             com.cebolao.lotofacil.data.FilterType.SOMA_DEZENAS,
             com.cebolao.lotofacil.data.FilterType.PARES,
             com.cebolao.lotofacil.data.FilterType.PRIMOS
         )
    }
    val basic = partitionResult.first
    val advanced = partitionResult.second

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = { TextButton(onClick = viewModel::requestResetFilters) { Text(stringResource(R.string.filters_reset_button_description)) } },
        bottomBar = { GenerationActionsPanel(uiState.generationState, viewModel::generateGames, viewModel::cancelGeneration) }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            item { FilterPresetSelector(viewModel::applyPreset, Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)) }
            item { HorizontalDivider(Modifier.padding(vertical = Dimen.ExtraSmallPadding), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            
            // Group Filters (Moved up)

            // Basic Section
            item {
                Text(
                    text = "Básico",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)
                )
            }
            items(basic, key = { it.type.name }) { filter ->
                FilterCard(filter, { viewModel.onFilterToggle(filter.type, it) }, { viewModel.onRangeAdjust(filter.type, it) }, { viewModel.showFilterInfo(filter.type) }, uiState.lastDraw, Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SpacingXS))
            }

            // Advanced Section
            item {
                Text(
                    text = "Avançado",
                    style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.tertiary, // Pink for advanced/secondary
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding).padding(top = Dimen.MediumPadding)
                )
            }
            items(advanced, key = { it.type.name }) { filter ->
                FilterCard(filter, { viewModel.onFilterToggle(filter.type, it) }, { viewModel.onRangeAdjust(filter.type, it) }, { viewModel.showFilterInfo(filter.type) }, uiState.lastDraw, Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SpacingXS))
            }
        }
    }
}