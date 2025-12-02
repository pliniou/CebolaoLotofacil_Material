package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.R // IMPORT ADICIONADO
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.*
import com.cebolao.lotofacil.util.WIDGET_UPDATE_INTERVAL_HOURS
import com.cebolao.lotofacil.util.WIDGET_UPDATE_WORK_NAME
import com.cebolao.lotofacil.widget.WidgetUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Stable
sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Error(@param:StringRes val messageResId: Int) : HomeScreenState
    data class Success(
        val lastDraw: HistoricalDraw?,
        val lastDrawSimpleStats: ImmutableList<Pair<String, String>>,
        val lastDrawCheckResult: CheckResult?,
        val nextDrawInfo: NextDrawInfo?,
        val winnerData: List<WinnerData>
    ) : HomeScreenState
}

@Stable
data class HomeUiState(
    val screenState: HomeScreenState = HomeScreenState.Loading,
    val statistics: StatisticsReport? = null,
    val isStatsLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val selectedPattern: StatisticPattern = StatisticPattern.SUM,
    val selectedTimeWindow: Int = 0,
    val syncMessageRes: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val checkGameUseCase: CheckGameUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _timeWindow = MutableStateFlow(0)

    init {
        observeSyncStatus()
        observeTimeWindowChange()
        reloadHomeData()
        scheduleWidgetUpdate()
    }

    private fun observeSyncStatus() {
        historyRepository.syncStatus.onEach { status ->
            _uiState.update { state ->
                state.copy(
                    isSyncing = status is SyncStatus.Syncing,
                    syncMessageRes = when(status) {
                        is SyncStatus.Success -> R.string.home_sync_success_message
                        is SyncStatus.Failed -> R.string.home_sync_failed_message
                        else -> null
                    }
                )
            }
            if (status is SyncStatus.Success) {
                reloadHomeData()
            }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTimeWindowChange() {
        _timeWindow
            .flatMapLatest { window ->
                _uiState.update { it.copy(isStatsLoading = true, selectedTimeWindow = window) }
                flow { emit(getAnalyzedStatsUseCase(window)) }
            }
            .onEach { result ->
                _uiState.update { state ->
                    result.fold(
                        onSuccess = { stats -> state.copy(statistics = stats, isStatsLoading = false) },
                        onFailure = { state.copy(isStatsLoading = false) }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun reloadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(screenState = HomeScreenState.Loading) }

            getHomeScreenDataUseCase()
                .collect { result -> processHomeDataResult(result) }
        }
    }

    private suspend fun processHomeDataResult(result: Result<HomeScreenData>) {
        result.fold(
            onSuccess = { data ->
                val lastDraw = data.lastDraw

                val simpleStats = lastDraw?.let {
                    getGameSimpleStatsUseCase(it).first().getOrNull()
                } ?: persistentListOf()

                val checkResult = lastDraw?.let {
                    checkGameUseCase(it.numbers).first().getOrNull()
                }

                _uiState.update {
                    it.copy(
                        screenState = HomeScreenState.Success(
                            lastDraw = lastDraw,
                            lastDrawSimpleStats = simpleStats,
                            lastDrawCheckResult = checkResult,
                            nextDrawInfo = data.nextDrawInfo,
                            winnerData = data.winnerData
                        ),
                        statistics = data.initialStats
                    )
                }
            },
            onFailure = {
                _uiState.update { it.copy(screenState = HomeScreenState.Error(R.string.error_load_data_failed)) }
            }
        )
    }

    private fun scheduleWidgetUpdate() {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(WIDGET_UPDATE_INTERVAL_HOURS, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun onMessageShown() {
        _uiState.update { it.copy(syncMessageRes = null) }
    }

    fun forceSync() = syncHistoryUseCase()

    fun onTimeWindowSelected(window: Int) {
        _timeWindow.value = window
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}