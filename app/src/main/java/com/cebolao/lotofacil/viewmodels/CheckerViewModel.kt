package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CheckerUiState {
    data object Idle : CheckerUiState
    data object Loading : CheckerUiState
    data class Success(val result: CheckResult, val simpleStats: ImmutableList<Pair<String, String>>) : CheckerUiState
    data class Error(@StringRes val messageResId: Int) : CheckerUiState
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

    // Deriva o estado de completude reativamente
    val isGameComplete = _selectedNumbers
        .map { it.size == LotofacilConstants.GAME_SIZE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), false)

    private val _events = Channel<Int>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Inicializa com argumentos de navegação, se houver
        savedStateHandle.get<String>(Screen.Checker.ARG_NUMBERS)?.let { arg ->
            val numbers = arg.split(CHECKER_ARG_SEPARATOR).mapNotNull { it.toIntOrNull() }.toSet()
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
        if (_uiState.value !is CheckerUiState.Idle) _uiState.value = CheckerUiState.Idle
    }

    fun clearNumbers() {
        _selectedNumbers.value = emptySet()
        _uiState.value = CheckerUiState.Idle
    }

    fun checkGame() {
        if (!isGameComplete.value) {
            sendEvent(R.string.checker_incomplete_game_message)
            return
        }

        viewModelScope.launch {
            _uiState.value = CheckerUiState.Loading
            analyzeGameUseCase(LotofacilGame(_selectedNumbers.value))
                .onSuccess { 
                    _uiState.value = CheckerUiState.Success(it.checkResult, it.simpleStats) 
                }
                .onFailure { 
                    _uiState.value = CheckerUiState.Error(R.string.error_analysis_failed) 
                }
        }
    }

    fun saveGame() {
        if (!isGameComplete.value) return
        viewModelScope.launch {
            saveGameUseCase(LotofacilGame(_selectedNumbers.value))
                .onSuccess { sendEvent(R.string.checker_save_success_message) }
                .onFailure { sendEvent(R.string.checker_save_fail_message) }
        }
    }

    private fun sendEvent(@StringRes resId: Int) {
        viewModelScope.launch { _events.send(resId) }
    }
}