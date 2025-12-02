package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R // IMPORT ADICIONADO
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import com.cebolao.lotofacil.domain.usecase.SaveGameUseCase
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.util.CHECKER_ARG_SEPARATOR
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
sealed interface CheckerUiEvent {
    data class ShowSnackbar(@get:StringRes val messageResId: Int) : CheckerUiEvent
}

@Stable
sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(val result: CheckResult, val simpleStats: ImmutableList<Pair<String, String>>) : CheckerUiState
    data class Error(@param:StringRes val messageResId: Int) : CheckerUiState
}

@HiltViewModel
class CheckerViewModel @Inject constructor(
    private val analyzeGameUseCase: AnalyzeGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckerUiState>(CheckerUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers = _selectedNumbers.asStateFlow()

    val isGameComplete = _selectedNumbers
        .map { it.size == LotofacilConstants.GAME_SIZE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), false)

    private val _eventFlow = MutableSharedFlow<CheckerUiEvent>()
    val events = _eventFlow.asSharedFlow()

    init {
        val numbersArg = savedStateHandle.get<String>(Screen.Checker.CHECKER_NUMBERS_ARG)
        if (!numbersArg.isNullOrBlank()) {
            val numbers = numbersArg.split(CHECKER_ARG_SEPARATOR)
                .mapNotNull { it.toIntOrNull() }
                .toSet()

            if (numbers.size == LotofacilConstants.GAME_SIZE) {
                _selectedNumbers.value = numbers
                checkGame()
            }
        }
    }

    fun toggleNumber(number: Int) {
        _selectedNumbers.update { current ->
            when {
                number in current -> current - number
                current.size < LotofacilConstants.GAME_SIZE -> current + number
                else -> current
            }
        }
        if (_uiState.value !is CheckerUiState.Idle) {
            _uiState.update { CheckerUiState.Idle }
        }
    }

    fun clearNumbers() {
        _selectedNumbers.update { emptySet() }
        _uiState.update { CheckerUiState.Idle }
    }

    fun checkGame() {
        if (!isGameComplete.value) {
            emitError(R.string.checker_incomplete_game_message)
            return
        }

        viewModelScope.launch {
            _uiState.update { CheckerUiState.Loading }
            val game = LotofacilGame(numbers = _selectedNumbers.value)

            analyzeGameUseCase(game)
                .onSuccess { result ->
                    _uiState.update { CheckerUiState.Success(result.checkResult, result.simpleStats) }
                }
                .onFailure {
                    _uiState.update { CheckerUiState.Error(R.string.error_analysis_failed) }
                }
        }
    }

    fun saveGame() {
        if (!isGameComplete.value) return

        viewModelScope.launch {
            val game = LotofacilGame(numbers = _selectedNumbers.value)
            saveGameUseCase(game)
                .onSuccess { emitError(R.string.checker_save_success_message) }
                .onFailure { emitError(R.string.checker_save_fail_message) }
        }
    }

    private fun emitError(@StringRes resId: Int) {
        viewModelScope.launch { _eventFlow.emit(CheckerUiEvent.ShowSnackbar(resId)) }
    }
}