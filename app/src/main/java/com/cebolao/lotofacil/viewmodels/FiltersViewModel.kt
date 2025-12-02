package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.service.FilterSuccessCalculator
import com.cebolao.lotofacil.domain.service.GenerationFailureReason
import com.cebolao.lotofacil.domain.service.GenerationProgress
import com.cebolao.lotofacil.domain.service.GenerationProgressType
import com.cebolao.lotofacil.domain.service.GenerationStep
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@Stable
sealed interface NavigationEvent {
    data object NavigateToGeneratedGames : NavigationEvent
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : NavigationEvent
}

@Stable
data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val successProbability: Float = 1f,
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null
)

@Stable
sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Loading(
        @param:StringRes val messageRes: Int,
        val progress: Int = 0,
        val total: Int = 0
    ) : GenerationUiState
}

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val saveGeneratedGamesUseCase: SaveGeneratedGamesUseCase,
    private val generateGamesUseCase: GenerateGamesUseCase,
    private val filterSuccessCalculator: FilterSuccessCalculator,
    private val getLastDrawUseCase: GetLastDrawUseCase
) : ViewModel() {

    private val _filterStates = MutableStateFlow(FilterType.entries.map { FilterState(type = it) })
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    private val _lastDraw = MutableStateFlow<Set<Int>?>(null)
    private val _showResetDialog = MutableStateFlow(false)
    private val _filterInfoToShow = MutableStateFlow<FilterType?>(null)

    private val _eventFlow = MutableSharedFlow<NavigationEvent>()
    val events = _eventFlow.asSharedFlow()

    private var generationJob: Job? = null

    private val _probability = _filterStates
        .map { filters -> filterSuccessCalculator(filters.filter { it.isEnabled }) }
        .distinctUntilChanged()

    // Combinação em etapas para garantir Type Safety (evitando cast não checado de List<Any>)
    private val _uiStateBase = combine(
        _filterStates,
        _generationState,
        _lastDraw
    ) { filters, genState, lastDraw ->
        Triple(filters, genState, lastDraw)
    }

    private val _uiStateOverlay = combine(
        _probability,
        _showResetDialog,
        _filterInfoToShow
    ) { prob, showDialog, info ->
        Triple(prob, showDialog, info)
    }

    val uiState: StateFlow<FiltersScreenState> = combine(
        _uiStateBase,
        _uiStateOverlay
    ) { (filters, genState, lastDraw), (prob, showDialog, info) ->
        FiltersScreenState(
            filterStates = filters,
            generationState = genState,
            lastDraw = lastDraw,
            successProbability = prob,
            showResetDialog = showDialog,
            filterInfoToShow = info
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
        initialValue = FiltersScreenState()
    )

    init {
        loadLastDraw()
    }

    private fun loadLastDraw() {
        viewModelScope.launch {
            getLastDrawUseCase().onSuccess { _lastDraw.value = it?.numbers }
        }
    }

    fun onFilterToggle(type: FilterType, isEnabled: Boolean) {
        _filterStates.update { states ->
            states.map { if (it.type == type) it.copy(isEnabled = isEnabled) else it }
        }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val snappedRange = newRange.snapToStep(type.fullRange)
        _filterStates.update { currentStates ->
            currentStates.map {
                if (it.type == type && it.selectedRange != snappedRange) it.copy(selectedRange = snappedRange) else it
            }
        }
    }

    fun applyPreset(preset: FilterPreset) {
        _filterStates.update { currentStates ->
            currentStates.map { filterState ->
                val rule = preset.rules[filterState.type]
                if (rule != null) {
                    filterState.copy(isEnabled = true, selectedRange = rule)
                } else {
                    filterState.copy(isEnabled = false, selectedRange = filterState.type.defaultRange)
                }
            }
        }
    }

    fun generateGames(quantity: Int) {
        if (_generationState.value is GenerationUiState.Loading) return

        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            generateGamesUseCase(quantity, _filterStates.value)
                .onCompletion { if (it is CancellationException) _generationState.value = GenerationUiState.Idle }
                .collect { handleGenerationProgress(it) }
        }
    }

    private suspend fun handleGenerationProgress(progress: GenerationProgress) {
        when (val type = progress.progressType) {
            is GenerationProgressType.Started -> updateLoading(R.string.general_loading, 0, progress.total)
            is GenerationProgressType.Step -> {
                val msgRes = when(type.step) {
                    GenerationStep.RANDOM_START -> R.string.game_generator_random_start
                    GenerationStep.HEURISTIC_START -> R.string.game_generator_heuristic_start
                    GenerationStep.RANDOM_FALLBACK -> R.string.game_generator_random_fallback
                }
                updateLoading(msgRes, progress.current, progress.total)
            }
            is GenerationProgressType.Attempt -> {
                val currentMsg = (_generationState.value as? GenerationUiState.Loading)?.messageRes ?: R.string.general_loading
                updateLoading(currentMsg, progress.current, progress.total)
            }
            is GenerationProgressType.Finished -> {
                saveGeneratedGamesUseCase(type.games)
                _eventFlow.emit(NavigationEvent.NavigateToGeneratedGames)
                _generationState.value = GenerationUiState.Idle
            }
            is GenerationProgressType.Failed -> {
                val errorRes = when(type.reason) {
                    GenerationFailureReason.NO_HISTORY -> R.string.game_generator_failure_no_history
                    GenerationFailureReason.GENERIC_ERROR -> R.string.game_generator_failure_generic
                }
                _eventFlow.emit(NavigationEvent.ShowSnackbar(errorRes))
                _generationState.value = GenerationUiState.Idle
            }
        }
    }

    private fun updateLoading(msgRes: Int, current: Int, total: Int) {
        _generationState.value = GenerationUiState.Loading(msgRes, current, total)
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        _generationState.value = GenerationUiState.Idle
    }

    fun requestResetFilters() { _showResetDialog.value = true }
    
    fun confirmResetFilters() {
        _filterStates.value = FilterType.entries.map { FilterState(type = it) }
        _showResetDialog.value = false
    }
    
    fun dismissResetDialog() { _showResetDialog.value = false }
    fun showFilterInfo(type: FilterType) { _filterInfoToShow.value = type }
    fun dismissFilterInfo() { _filterInfoToShow.value = null }
}

private fun ClosedFloatingPointRange<Float>.snapToStep(full: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float> {
    val start = this.start.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)
    val end = this.endInclusive.roundToInt().toFloat().coerceIn(full.start, full.endInclusive)
    return start..end
}