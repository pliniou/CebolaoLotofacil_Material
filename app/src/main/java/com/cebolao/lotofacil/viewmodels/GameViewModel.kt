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
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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
    data class Error(@StringRes val messageResId: Int) : GameAnalysisUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val analyzeGameUseCase: AnalyzeGameUseCase
) : ViewModel() {

    val unpinnedGames = gameRepository.unpinnedGames
    val pinnedGames = gameRepository.pinnedGames

    private val _gameToDelete = MutableStateFlow<LotofacilGame?>(null)
    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)
    private val _eventFlow = MutableSharedFlow<GameScreenEvent>()
    
    val analysisState = _analysisState.asStateFlow()
    val events = _eventFlow.asSharedFlow()

    val uiState: StateFlow<GameScreenUiState> = combine(
        unpinnedGames,
        pinnedGames,
        _gameToDelete
    ) { unpinned, pinned, gameToDelete ->
        val total = unpinned.size + pinned.size
        
        GameScreenUiState(
            gameToDelete = gameToDelete,
            summary = GameSummary(
                totalGames = total,
                pinnedGames = pinned.size,
                totalCost = LotofacilConstants.GAME_COST.multiply(BigDecimal(total))
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), GameScreenUiState())

    private var analyzeJob: Job? = null

    fun togglePinState(game: LotofacilGame) = viewModelScope.launch {
        gameRepository.togglePinState(game)
    }

    fun requestDeleteGame(game: LotofacilGame) {
        _gameToDelete.value = game
    }

    fun confirmDeleteGame() {
        _gameToDelete.value?.let { game ->
            viewModelScope.launch {
                gameRepository.deleteGame(game)
                _gameToDelete.value = null
            }
        }
    }

    fun dismissDeleteDialog() { _gameToDelete.value = null }

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