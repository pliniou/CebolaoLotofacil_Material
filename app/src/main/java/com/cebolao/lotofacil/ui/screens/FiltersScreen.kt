package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.components.AppConfirmationDialog
import com.cebolao.lotofacil.ui.components.FilterCard
import com.cebolao.lotofacil.ui.components.FilterPresetSelector
import com.cebolao.lotofacil.ui.components.GenerationActionsPanel
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.InfoPoint
import com.cebolao.lotofacil.ui.components.SectionCard
import com.cebolao.lotofacil.ui.components.SectionHeader
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
                        context.getString(
                            event.messageRes,
                            context.getString(event.labelRes)
                        )
                    } else {
                        context.getString(event.messageRes)
                    }
                    snackbarHostState.showSnackbar(message)
                }
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
            dialogTitle = stringResource(
                R.string.filters_info_dialog_title_format,
                stringResource(type.titleRes)
            ),
            icon = type.filterIcon,
            onDismissRequest = viewModel::dismissFilterInfo
        ) {
            SectionCard {
                InfoPoint(
                    title = stringResource(R.string.filters_info_button_description),
                    description = stringResource(type.descriptionRes)
                )
            }
        }
    }

    val allFilters: List<FilterState> = uiState.filterStates
    val (basic, advanced) = allFilters.partition {
        it.type in listOf(
            FilterType.SOMA_DEZENAS,
            FilterType.PARES,
            FilterType.PRIMOS
        )
    }

    AppScreen(
        title = stringResource(R.string.filters_title),
        subtitle = stringResource(R.string.filters_subtitle),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            TextButton(onClick = viewModel::requestResetFilters) {
                Text(stringResource(R.string.filters_reset_button_description))
            }
        },
        bottomBar = {
            GenerationActionsPanel(
                uiState.generationState,
                viewModel::generateGames,
                viewModel::cancelGeneration
            )
        }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            // Preset Selector (configuração rápida)
            item {
                FilterPresetSelector(
                    onPresetSelected = viewModel::applyPreset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimen.SpacingShort)
                )
            }

            // Card de força/impacto dos filtros
            item {
                FilterSuccessCard(
                    probability = uiState.successProbability,
                    modifier = Modifier.padding(vertical = Dimen.SpacingMedium)
                )
            }

            item {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = Dimen.Border.Hairline
                )
            }

            // Seção Básico
            item {
                SectionHeader("Básico")
            }
            items(basic, key = { it.type.name }) { filter ->
                FilterCard(
                    state = filter,
                    onToggle = { viewModel.onFilterToggle(filter.type, it) },
                    onRange = { viewModel.onRangeAdjust(filter.type, it) },
                    onInfo = { viewModel.showFilterInfo(filter.type) },
                    lastDraw = uiState.lastDraw,
                    modifier = Modifier.padding(vertical = Dimen.SpacingMedium)
                )
            }

            // Seção Avançado
            item {
                SectionHeader("Avançado")
            }
            items(advanced, key = { it.type.name }) { filter ->
                FilterCard(
                    state = filter,
                    onToggle = { viewModel.onFilterToggle(filter.type, it) },
                    onRange = { viewModel.onRangeAdjust(filter.type, it) },
                    onInfo = { viewModel.showFilterInfo(filter.type) },
                    lastDraw = uiState.lastDraw,
                    modifier = Modifier.padding(vertical = Dimen.SpacingMedium)
                )
            }
        }
    }
}

@Composable
private fun FilterSuccessCard(
    probability: Float,
    modifier: Modifier = Modifier
) {
    val clampedProbability = probability.coerceIn(0f, 1f)
    val percentageLabel = String.format("%.0f%%", clampedProbability * 100f)

    SectionCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Força dos filtros",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = percentageLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LinearProgressIndicator(
                progress = { clampedProbability },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Estimativa de quantidade de jogos gerados que passam pelos filtros selecionados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
