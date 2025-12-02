package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.usecase.AnalyzeGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@Stable
sealed interface GameScreenEvent {
    data class ShareGame(val numbers: List<Int>) : GameScreenEvent
}

@Stable
data class GameSummary(
    val totalGames: Int = 0,
    val pinnedGames: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO
)

@Stable
data class GameScreenUiState(
    val gameToDelete: LotofacilGame? = null,
    val summary: GameSummary = GameSummary()
)

@Stable
data class GameAnalysisResult(
    val game: LotofacilGame,
    val simpleStats: ImmutableList<Pair<String, String>>,
    val checkResult: CheckResult
)

@Stable
sealed interface GameAnalysisUiState {
    data object Idle : GameAnalysisUiState
    data object Loading : GameAnalysisUiState
    data class Success(val result: GameAnalysisResult) : GameAnalysisUiState
    data class Error(@param:StringRes val messageResId: Int) : GameAnalysisUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val analyzeGameUseCase: AnalyzeGameUseCase
) : ViewModel() {

    val unpinnedGames = gameRepository.unpinnedGames
    val pinnedGames = gameRepository.pinnedGames

    private val _uiState = MutableStateFlow(GameScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)
    val analysisState = _analysisState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<GameScreenEvent>()
    val events = _eventFlow.asSharedFlow()

    private var analyzeJob: Job? = null

    init {
        viewModelScope.launch {
            combine(unpinnedGames, pinnedGames) { unpinned, pinned ->
                val total = unpinned.size + pinned.size
                GameSummary(
                    totalGames = total,
                    pinnedGames = pinned.size,
                    totalCost = LotofacilConstants.GAME_COST.multiply(BigDecimal(total))
                )
            }.collect { summary ->
                _uiState.update { it.copy(summary = summary) }
            }
        }
    }

    fun togglePinState(game: LotofacilGame) = viewModelScope.launch {
        gameRepository.togglePinState(game)
    }

    fun requestDeleteGame(game: LotofacilGame) {
        _uiState.update { it.copy(gameToDelete = game) }
    }

    fun confirmDeleteGame() {
        _uiState.value.gameToDelete?.let { game ->
            viewModelScope.launch {
                gameRepository.deleteGame(game)
                _uiState.update { it.copy(gameToDelete = null) }
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(gameToDelete = null) }
    }

    fun clearUnpinned() = viewModelScope.launch {
        gameRepository.clearUnpinnedGames()
    }

    fun analyzeGame(game: LotofacilGame) {
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _analysisState.value = GameAnalysisUiState.Loading
            analyzeGameUseCase(game)
                .onSuccess { _analysisState.value = GameAnalysisUiState.Success(it) }
                .onFailure { _analysisState.value = GameAnalysisUiState.Error(R.string.error_analysis_failed) }
        }
    }

    fun dismissAnalysisDialog() {
        analyzeJob?.cancel()
        _analysisState.value = GameAnalysisUiState.Idle
    }

    fun shareGame(game: LotofacilGame) = viewModelScope.launch {
        _eventFlow.emit(GameScreenEvent.ShareGame(game.numbers.sorted()))
    }
}