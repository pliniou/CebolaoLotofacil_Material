package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.LotofacilConstants
// Import atualizado para o novo pacote de domínio
import com.cebolao.lotofacil.domain.model.LotofacilGame 
import com.cebolao.lotofacil.domain.usecase.*
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

sealed interface GameScreenEvent {
    data class ShareGame(val numbers: List<Int>) : GameScreenEvent
}

data class GameSummary(
    val totalGames: Int = 0,
    val pinnedGames: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO
)

data class GameScreenUiState(
    val gameToDelete: LotofacilGame? = null,
    val summary: GameSummary = GameSummary()
)

data class GameAnalysisResult(
    val game: LotofacilGame,
    val simpleStats: ImmutableList<Pair<String, String>>,
    val checkResult: CheckResult
)

sealed interface GameAnalysisUiState {
    data object Idle : GameAnalysisUiState
    data object Loading : GameAnalysisUiState
    data class Success(val result: GameAnalysisResult) : GameAnalysisUiState
    data class Error(@param:StringRes val messageResId: Int) : GameAnalysisUiState
}

@HiltViewModel
class GameViewModel @Inject constructor(
    observeUnpinnedGamesUseCase: ObserveUnpinnedGamesUseCase,
    observePinnedGamesUseCase: ObservePinnedGamesUseCase,
    private val togglePinStateUseCase: TogglePinStateUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val clearUnpinnedGamesUseCase: ClearUnpinnedGamesUseCase,
    private val analyzeGameUseCase: AnalyzeGameUseCase
) : ViewModel() {

    val unpinnedGames = observeUnpinnedGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), kotlinx.collections.immutable.persistentListOf())

    val pinnedGames = observePinnedGamesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), kotlinx.collections.immutable.persistentListOf())

    private val _gameToDelete = MutableStateFlow<LotofacilGame?>(null)
    private val _analysisState = MutableStateFlow<GameAnalysisUiState>(GameAnalysisUiState.Idle)
    private val _events = Channel<GameScreenEvent>(Channel.BUFFERED)
    
    val analysisState = _analysisState.asStateFlow()
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<GameScreenUiState> = combine(
        unpinnedGames, pinnedGames, _gameToDelete
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
        togglePinStateUseCase(game)
    }

    fun requestDeleteGame(game: LotofacilGame) { _gameToDelete.value = game }
    fun dismissDeleteDialog() { _gameToDelete.value = null }

    fun confirmDeleteGame() {
        _gameToDelete.value?.let { game ->
            viewModelScope.launch {
                deleteGameUseCase(game)
                _gameToDelete.value = null
            }
        }
    }

    fun clearUnpinned() = viewModelScope.launch {
        clearUnpinnedGamesUseCase()
    }

    fun analyzeGame(game: LotofacilGame) {
        if (_analysisState.value is GameAnalysisUiState.Loading) return
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

    fun shareGame(game: LotofacilGame) {
        viewModelScope.launch { _events.send(GameScreenEvent.ShareGame(game.numbers.sorted())) }
    }
}