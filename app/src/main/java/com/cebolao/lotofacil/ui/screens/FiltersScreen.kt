package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.FilterPresetSelector
import com.cebolao.lotofacil.ui.components.FilterStatsPanel
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.InfoPoint
import com.cebolao.lotofacil.ui.components.SectionCard
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
            when (event) {
                is NavigationEvent.NavigateToGeneratedGames -> {
                    navController.navigate(Screen.GeneratedGames.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is NavigationEvent.ShowSnackbar -> { }
            }
        }
    }

    if (uiState.showResetDialog) {
        AppConfirmationDialog(
            title = R.string.filters_reset_dialog_title,
            message = R.string.filters_reset_dialog_message,
            confirmText = R.string.filters_reset_confirm,
            onConfirm = viewModel::confirmResetFilters,
            onDismiss = viewModel::dismissResetDialog,
            icon = Icons.Default.DeleteSweep
        )
    }

    uiState.filterInfoToShow?.let { type ->
        InfoDialog(
            dialogTitle = stringResource(R.string.filters_info_dialog_title_format, stringResource(type.titleRes)),
            icon = type.filterIcon,
            onDismissRequest = viewModel::dismissFilterInfo
        ) {
            SectionCard {
                InfoPoint(
                    title = "Definição",
                    description = "Ajuste o intervalo desejado para este parâmetro estatístico."
                )
            }
        }
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        actions = {
            TextButton(onClick = viewModel::requestResetFilters) {
                Text(stringResource(R.string.filters_reset_button_description))
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimen.BottomBarSpacer)
        ) {
            // Header: Presets
            item {
                FilterPresetSelector(
                    onPresetSelected = viewModel::applyPreset,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.SmallPadding)
                )
            }

            // Header: Stats
            item {
                FilterStatsPanel(
                    activeFilters = uiState.filterStates.filter { it.isEnabled },
                    successProbability = uiState.successProbability,
                    modifier = Modifier.padding(horizontal = Dimen.ScreenPadding)
                )
            }

            item {
                HorizontalDivider(
                    Modifier.padding(vertical = Dimen.SmallPadding),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            // Lista de Filtros
            items(
                items = uiState.filterStates,
                key = { it.type.name }
            ) { filter ->
                FilterItemRow(
                    filterState = filter,
                    onToggle = { isEnabled -> viewModel.onFilterToggle(filter.type, isEnabled) },
                    onRangeChange = { range -> viewModel.onRangeAdjust(filter.type, range) },
                    onInfoClick = { viewModel.showFilterInfo(filter.type) }
                )
            }
        }
    }
}

@Composable
fun FilterItemRow(
    filterState: FilterState,
    onToggle: (Boolean) -> Unit,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfoClick: () -> Unit,
    lastDrawNumbers: Set<Int>? = null
) {
    val isDataMissing by remember(filterState.type, lastDrawNumbers) {
        derivedStateOf {
            filterState.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR && lastDrawNumbers == null
        }
    }

    val isActive = filterState.isEnabled && !isDataMissing
    val contentAlpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.6f, label = "alpha")

    SectionCard(
        modifier = Modifier.padding(horizontal = Dimen.ScreenPadding, vertical = Dimen.ExtraSmallPadding),
        backgroundColor = if(isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(Dimen.SmallPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
            ) {
                Icon(
                    imageVector = filterState.type.filterIcon,
                    contentDescription = null,
                    tint = if(isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Dimen.MediumIcon)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(filterState.type.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isDataMissing) {
                        Text(
                            text = stringResource(R.string.filters_unavailable_data),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = filterState.isEnabled,
                    onCheckedChange = onToggle,
                    enabled = !isDataMissing
                )
            }

            AnimatedVisibility(
                visible = isActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = Dimen.MediumPadding).alpha(contentAlpha)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.SmallPadding),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mín: ${filterState.selectedRange.start.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Máx: ${filterState.selectedRange.endInclusive.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    RangeSlider(
                        value = filterState.selectedRange,
                        onValueChange = onRangeChange,
                        valueRange = filterState.type.fullRange,
                        steps = (filterState.type.fullRange.endInclusive - filterState.type.fullRange.start).toInt() - 1
                    )
                }
            }
        }
    }
}