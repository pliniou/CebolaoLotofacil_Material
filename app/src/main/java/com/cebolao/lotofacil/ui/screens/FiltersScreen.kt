package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.filterPresets
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
import com.cebolao.lotofacil.viewmodels.FiltersScreenState
import com.cebolao.lotofacil.viewmodels.FiltersViewModel
import com.cebolao.lotofacil.viewmodels.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FiltersScreen(
    navController: NavController,
    viewModel: FiltersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observa eventos de navegação e snackbar
    LaunchedEffect(key1 = true) {
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
                    // Agora resolvemos o ID da string aqui na UI se necessário,
                    // mas o evento refatorado já traz o ID.
                    // Nota: O evento foi definido como NavigationEvent.ShowSnackbar(@StringRes val messageRes: Int)
                    // Precisamos garantir que estamos extraindo a string corretamente.
                    // Como estamos num contexto de coroutine, precisamos do contexto ou helper.
                    // SIMPLIFICAÇÃO: O evento pode ter mudado. Vamos assumir a versão da Fase 3.
                }
            }
        }
    }

    // Tratamento separado para Snackbar com String Resource (da refatoração Fase 3)
    // Precisamos de um contexto composable para stringResource, mas LaunchedEffect roda em coroutine.
    // Solução: Mapear o ID para texto dentro do LaunchedEffect usando o context.
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(true) {
        viewModel.events.collectLatest { event ->
            if (event is NavigationEvent.ShowSnackbar) {
                snackbarHostState.showSnackbar(context.getString(event.messageRes))
            }
        }
    }

    // Diálogos
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
        FilterInfoDialog(filterType, viewModel::dismissFilterInfo)
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        navigationIcon = { Icon(Icons.Filled.Tune, stringResource(R.string.filters_title), tint = MaterialTheme.colorScheme.primary) },
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
        FiltersContent(uiState, viewModel, innerPadding)
    }
}

@Composable
private fun FiltersContent(
    uiState: FiltersScreenState,
    viewModel: FiltersViewModel,
    paddingValues: PaddingValues
) {
    StandardPageLayout(contentPadding = paddingValues) {
        item {
            AnimateOnEntry {
                FilterStatsPanel(
                    activeFilters = uiState.filterStates.filter { it.isEnabled },
                    successProbability = uiState.successProbability
                )
            }
        }

        item {
            AnimateOnEntry(delayMillis = 100) {
                FilterPresetSelector(onPresetSelected = viewModel::applyPreset)
            }
        }

        items(uiState.filterStates, key = { it.type.name }) { filter ->
            AnimateOnEntry {
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
}

@Composable
private fun FilterPresetSelector(
    onPresetSelected: (FilterPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.ExtraSmallPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.filters_presets_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.filters_presets_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
            items(filterPresets) { preset ->
                Card(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onPresetSelected(preset) }
                        )
                        .size(width = Dimen.PaletteCardWidth * 1.3f, height = Dimen.PaletteCardHeight * 0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(Dimen.Elevation.Low),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(Dimen.MediumPadding).fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            preset.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            preset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            minLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterInfoDialog(filterType: FilterType, onDismiss: () -> Unit) {
    InfoDialog(
        dialogTitle = stringResource(id = R.string.filters_info_dialog_title_format, filterType.title),
        icon = Icons.Default.Info,
        onDismissRequest = onDismiss
    ) {
        FormattedText(text = filterType.description)
    }
}