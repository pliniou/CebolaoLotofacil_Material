package com.cebolao.lotofacil.viewmodels

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.CheckGameUseCase
import com.cebolao.lotofacil.domain.usecase.GetAnalyzedStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import com.cebolao.lotofacil.util.WIDGET_UPDATE_INTERVAL_HOURS
import com.cebolao.lotofacil.util.WIDGET_UPDATE_WORK_NAME
import com.cebolao.lotofacil.widget.WidgetUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    val showSyncFailedMessage: Boolean = false,
    val showSyncSuccessMessage: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository, // Direto, sem ObserveSyncStatusUseCase
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    private val checkGameUseCase: CheckGameUseCase,
    private val workManager: WorkManager,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var analysisJob: Job? = null

    init {
        observeSync()
        loadData()
        scheduleWidgetUpdate()
    }

    private fun observeSync() {
        viewModelScope.launch {
            historyRepository.syncStatus.collect { status ->
                _uiState.update { 
                    it.copy(
                        isSyncing = status is SyncStatus.Syncing,
                        showSyncFailedMessage = status is SyncStatus.Failed,
                        showSyncSuccessMessage = status is SyncStatus.Success
                    ) 
                }
                if (status is SyncStatus.Success) loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isStatsLoading = true) } // Mantém loading visual dos stats

            getHomeScreenDataUseCase().collect { result ->
                result.onSuccess { data ->
                    val simpleStats = data.lastDraw?.let { getGameSimpleStatsUseCase(it).first().getOrNull() } ?: persistentListOf()
                    val checkResult = data.lastDraw?.let { checkGameUseCase(it.numbers).first().getOrNull() }

                    _uiState.update {
                        it.copy(
                            screenState = HomeScreenState.Success(
                                lastDraw = data.lastDraw,
                                lastDrawSimpleStats = simpleStats,
                                lastDrawCheckResult = checkResult,
                                nextDrawInfo = data.nextDrawInfo,
                                winnerData = data.winnerData
                            ),
                            statistics = data.initialStats,
                            isStatsLoading = false
                        )
                    }
                }.onFailure {
                    _uiState.update { it.copy(screenState = HomeScreenState.Error(R.string.error_load_data_failed), isStatsLoading = false) }
                }
            }
        }
    }

    private fun scheduleWidgetUpdate() {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(WIDGET_UPDATE_INTERVAL_HOURS, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(WIDGET_UPDATE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun onSyncMessageShown() = _uiState.update { it.copy(showSyncFailedMessage = false) }
    fun onSyncSuccessMessageShown() = _uiState.update { it.copy(showSyncSuccessMessage = false) }
    fun retryInitialLoad() = loadData()
    
    fun forceSync() {
        if (!_uiState.value.isSyncing) historyRepository.syncHistory()
    }

    fun onTimeWindowSelected(window: Int) {
        if (_uiState.value.selectedTimeWindow == window) return
        
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isStatsLoading = true, selectedTimeWindow = window) }
            
            getAnalyzedStatsUseCase(window).onSuccess { stats ->
                _uiState.update { it.copy(statistics = stats, isStatsLoading = false) }
            }.onFailure {
                _uiState.update { it.copy(isStatsLoading = false) }
            }
        }
    }

    fun onPatternSelected(pattern: StatisticPattern) {
        _uiState.update { it.copy(selectedPattern = pattern) }
    }
}