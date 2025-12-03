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
import com.cebolao.lotofacil.domain.model.HomeScreenData
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.domain.model.StatisticPattern
import com.cebolao.lotofacil.domain.model.WinnerData
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import com.cebolao.lotofacil.domain.usecase.GetAnalyzedStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetGameSimpleStatsUseCase
import com.cebolao.lotofacil.domain.usecase.GetHomeScreenDataUseCase
import com.cebolao.lotofacil.domain.usecase.SyncHistoryUseCase
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import com.cebolao.lotofacil.util.WIDGET_UPDATE_INTERVAL_HOURS
import com.cebolao.lotofacil.util.WIDGET_UPDATE_WORK_NAME
import com.cebolao.lotofacil.widget.WidgetUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
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

private data class HomeInputs(
    val timeWindow: Int,
    val pattern: StatisticPattern,
    val syncMessage: Int?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    historyRepository: HistoryRepository,
    private val syncHistoryUseCase: SyncHistoryUseCase,
    private val getHomeScreenDataUseCase: GetHomeScreenDataUseCase,
    private val getAnalyzedStatsUseCase: GetAnalyzedStatsUseCase,
    private val getGameSimpleStatsUseCase: GetGameSimpleStatsUseCase,
    // checkGameUseCase removido, agora está encapsulado no GetHomeScreenDataUseCase
    private val workManager: WorkManager
) : ViewModel() {

    private val _selectedTimeWindow = MutableStateFlow(0)
    private val _selectedPattern = MutableStateFlow(StatisticPattern.SUM)
    private val _syncMessageEvent = MutableStateFlow<Int?>(null)

    private val inputsFlow = combine(
        _selectedTimeWindow,
        _selectedPattern,
        _syncMessageEvent
    ) { timeWindow, pattern, msg ->
        HomeInputs(timeWindow, pattern, msg)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val homeDataFlow = historyRepository.syncStatus
        .flatMapLatest { getHomeScreenDataUseCase() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS),
            Result.success<HomeScreenData?>(null)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val statsFlow = _selectedTimeWindow
        .flatMapLatest { window ->
            flow {
                emit(getAnalyzedStatsUseCase(window))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), Result.success(StatisticsReport()))

    val uiState: StateFlow<HomeUiState> = combine(
        homeDataFlow,
        statsFlow,
        historyRepository.syncStatus,
        inputsFlow
    ) { homeResult, statsResult, syncStatus, inputs ->

        val screenState = homeResult.fold(
            onSuccess = { data ->
                if (data == null) HomeScreenState.Loading else processSuccessState(data)
            },
            onFailure = { HomeScreenState.Error(R.string.error_load_data_failed) }
        )

        val (stats, isStatsLoading) = statsResult.fold(
            onSuccess = { it to false },
            onFailure = { null to true }
        )

        val finalSyncMsg = inputs.syncMessage ?: when(syncStatus) {
            is SyncStatus.Success -> R.string.home_sync_success_message
            is SyncStatus.Failed -> R.string.home_sync_failed_message
            else -> null
        }

        HomeUiState(
            screenState = screenState,
            statistics = stats,
            isStatsLoading = isStatsLoading,
            isSyncing = syncStatus is SyncStatus.Syncing,
            selectedPattern = inputs.pattern,
            selectedTimeWindow = inputs.timeWindow,
            syncMessageRes = finalSyncMsg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        scheduleWidgetUpdate()
    }

    private fun processSuccessState(data: HomeScreenData): HomeScreenState.Success {
        // Estatísticas simples agora são calculadas de forma síncrona
        val simpleStats = data.lastDraw?.let {
            getGameSimpleStatsUseCase(it)
        } ?: persistentListOf()

        return HomeScreenState.Success(
            lastDraw = data.lastDraw,
            lastDrawSimpleStats = simpleStats,
            // CheckResult já vem pronto do UseCase, sem blocking calls aqui
            lastDrawCheckResult = data.lastDrawCheckResult,
            nextDrawInfo = data.nextDrawInfo,
            winnerData = data.winnerData
        )
    }

    private fun scheduleWidgetUpdate() {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            WIDGET_UPDATE_INTERVAL_HOURS, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun forceSync() { syncHistoryUseCase() }
    fun onMessageShown() { _syncMessageEvent.value = null }
    fun onTimeWindowSelected(window: Int) { _selectedTimeWindow.value = window }
    fun onPatternSelected(pattern: StatisticPattern) { _selectedPattern.value = pattern }
}