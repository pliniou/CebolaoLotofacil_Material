package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.datasource.HistoryLocalDataSource
import com.cebolao.lotofacil.data.datasource.HistoryRemoteDataSource
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.domain.repository.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepository"

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @param:ApplicationScope private val scope: CoroutineScope
) : HistoryRepository {

    private val writeMutex = Mutex()

    @Volatile
    private var cachedHistory: List<HistoricalDraw> = emptyList()
    
    @Volatile 
    private var latestApiResult: LotofacilApiResult? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        scope.launch {
            val local = localDataSource.getLocalHistory()
            updateCache(local)
            syncHistory()
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        if (cachedHistory.isEmpty()) {
            writeMutex.withLock {
                if (cachedHistory.isEmpty()) {
                    updateCache(localDataSource.getLocalHistory())
                }
            }
        }
        return cachedHistory
    }

    override suspend fun getLastDraw(): HistoricalDraw? {
        val fromApi = latestApiResult?.let { HistoricalDraw.fromApiResult(it) }
        return fromApi ?: cachedHistory.firstOrNull()
    }

    override suspend fun getLatestApiResult(): LotofacilApiResult? {
        return latestApiResult ?: runCatching { 
            remoteDataSource.getLatestDraw()?.also { latestApiResult = it } 
        }.getOrNull()
    }

    override fun syncHistory(): Job = scope.launch {
        if (_syncStatus.value is SyncStatus.Syncing || !writeMutex.tryLock()) return@launch

        try {
            _syncStatus.value = SyncStatus.Syncing
            performSync()
            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e)
        } finally {
            writeMutex.unlock()
        }
    }

    private suspend fun performSync() {
        val currentMax = cachedHistory.firstOrNull()?.contestNumber ?: 0
        
        val remoteResult = remoteDataSource.getLatestDraw() 
            ?: throw IllegalStateException("Unable to fetch latest draw from API")
        
        latestApiResult = remoteResult
        val remoteMax = remoteResult.numero

        if (remoteMax > currentMax) {
            val newDraws = remoteDataSource.getDrawsInRange((currentMax + 1)..remoteMax)
            if (newDraws.isNotEmpty()) {
                localDataSource.saveNewContests(newDraws)
                updateCache(newDraws + cachedHistory)
            }
        }
    }

    private fun updateCache(allDraws: List<HistoricalDraw>) {
        cachedHistory = allDraws
            .distinctBy { it.contestNumber }
            .sortedByDescending { it.contestNumber }
    }
}