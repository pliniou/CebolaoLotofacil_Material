package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterPreset
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.domain.service.*
import com.cebolao.lotofacil.domain.usecase.GenerateGamesUseCase
import com.cebolao.lotofacil.domain.usecase.GetLastDrawUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGeneratedGamesUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

sealed interface NavigationEvent {
    data object NavigateToGeneratedGames : NavigationEvent
    data class ShowSnackbar(@param:StringRes val messageRes: Int) : NavigationEvent
}

data class FiltersScreenState(
    val filterStates: List<FilterState> = emptyList(),
    val generationState: GenerationUiState = GenerationUiState.Idle,
    val lastDraw: Set<Int>? = null,
    val successProbability: Float = 1f,
    val showResetDialog: Boolean = false,
    val filterInfoToShow: FilterType? = null
)

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data class Loading(@param:StringRes val messageRes: Int, val progress: Int = 0, val total: Int = 0) : GenerationUiState
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

    private val _events = Channel<NavigationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var generationJob: Job? = null

    val uiState: StateFlow<FiltersScreenState> = combine(
        _filterStates, _generationState, _lastDraw, _showResetDialog, _filterInfoToShow
    ) { filters, genState, lastDraw, showReset, infoDialog ->
        FiltersScreenState(
            filterStates = filters,
            generationState = genState,
            lastDraw = lastDraw,
            successProbability = filterSuccessCalculator(filters.filter { it.isEnabled }),
            showResetDialog = showReset,
            filterInfoToShow = infoDialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), FiltersScreenState())

    init {
        viewModelScope.launch {
            getLastDrawUseCase().onSuccess { _lastDraw.value = it?.numbers }
        }
    }

    fun onFilterToggle(type: FilterType, isEnabled: Boolean) {
        updateFilter(type) { it.copy(isEnabled = isEnabled) }
    }

    fun onRangeAdjust(type: FilterType, newRange: ClosedFloatingPointRange<Float>) {
        val snapped = newRange.snapToStep(type.fullRange)
        updateFilter(type) { if (it.selectedRange != snapped) it.copy(selectedRange = snapped) else it }
    }

    fun applyPreset(preset: FilterPreset) {
        _filterStates.update { current ->
            current.map { state ->
                val rule = preset.rules[state.type]
                if (rule != null) state.copy(isEnabled = true, selectedRange = rule)
                else state.copy(isEnabled = false, selectedRange = state.type.defaultRange)
            }
        }
    }

    fun generateGames(quantity: Int) {
        if (_generationState.value is GenerationUiState.Loading) return
        generationJob?.cancel()
        
        generationJob = viewModelScope.launch {
            generateGamesUseCase(quantity, _filterStates.value).collect { progress ->
                handleProgress(progress)
            }
        }
    }

    private suspend fun handleProgress(progress: GenerationProgress) {
        when (val type = progress.progressType) {
            is GenerationProgressType.Started -> updateLoading(R.string.general_loading, 0, progress.total)
            is GenerationProgressType.Step -> updateLoading(
                when (type.step) {
                    GenerationStep.RANDOM_START -> R.string.game_generator_random_start
                    GenerationStep.HEURISTIC_START -> R.string.game_generator_heuristic_start
                    GenerationStep.RANDOM_FALLBACK -> R.string.game_generator_random_fallback
                }, progress.current, progress.total
            )
            is GenerationProgressType.Attempt -> {
                val currentMsg = (_generationState.value as? GenerationUiState.Loading)?.messageRes ?: R.string.general_loading
                updateLoading(currentMsg, progress.current, progress.total)
            }
            is GenerationProgressType.Finished -> {
                saveGeneratedGamesUseCase(type.games)
                _events.send(NavigationEvent.NavigateToGeneratedGames)
                _generationState.value = GenerationUiState.Idle
            }
            is GenerationProgressType.Failed -> {
                _events.send(NavigationEvent.ShowSnackbar(
                    if (type.reason == GenerationFailureReason.NO_HISTORY) R.string.game_generator_failure_no_history 
                    else R.string.game_generator_failure_generic
                ))
                _generationState.value = GenerationUiState.Idle
            }
        }
    }

    private fun updateFilter(type: FilterType, transform: (FilterState) -> FilterState) {
        _filterStates.update { list -> list.map { if (it.type == type) transform(it) else it } }
    }

    private fun updateLoading(@StringRes msg: Int, cur: Int, tot: Int) {
        _generationState.value = GenerationUiState.Loading(msg, cur, tot)
    }

    fun cancelGeneration() {
        generationJob?.cancel()
        _generationState.value = GenerationUiState.Idle
    }

    // Dialog Control
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