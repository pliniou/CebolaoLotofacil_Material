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
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRepository"

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val localDataSource: HistoryLocalDataSource,
    private val remoteDataSource: HistoryRemoteDataSource,
    @param:ApplicationScope private val scope: CoroutineScope
) : HistoryRepository {

    private val cacheMutex = Mutex()
    private val historyCache = Collections.synchronizedMap(mutableMapOf<Int, HistoricalDraw>())
    @Volatile private var latestApiResult: LotofacilApiResult? = null

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        scope.launch {
            loadLocalHistoryToCache()
            syncHistory()
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        if (historyCache.isEmpty()) loadLocalHistoryToCache()
        
        return cacheMutex.withLock {
            historyCache.values.sortedByDescending { it.contestNumber }
        }
    }

    override suspend fun getLastDraw(): HistoricalDraw? {
        latestApiResult?.let { apiResult ->
            HistoricalDraw.fromApiResult(apiResult)?.let { return it }
        }
        return getHistory().firstOrNull()
    }

    override suspend fun getLatestApiResult(): LotofacilApiResult? {
        return latestApiResult ?: runCatching { 
            remoteDataSource.getLatestDraw()?.also { latestApiResult = it } 
        }.getOrNull()
    }

    override fun syncHistory(): Job = scope.launch {
        if (_syncStatus.value is SyncStatus.Syncing) return@launch
        
        _syncStatus.value = SyncStatus.Syncing
        
        try {
            val localMax = historyCache.keys.maxOrNull() ?: 0
            val remoteResult = remoteDataSource.getLatestDraw() 
                ?: throw IllegalStateException("Failed to fetch latest draw")
            
            latestApiResult = remoteResult
            val remoteMax = remoteResult.numero

            if (remoteMax > localMax) {
                val newDraws = remoteDataSource.getDrawsInRange((localMax + 1)..remoteMax)
                if (newDraws.isNotEmpty()) {
                    localDataSource.saveNewContests(newDraws)
                    updateCache(newDraws)
                }
            }
            _syncStatus.value = SyncStatus.Success
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sync failed", e)
            _syncStatus.value = SyncStatus.Failed(e)
        }
    }

    private suspend fun loadLocalHistoryToCache() {
        val local = localDataSource.getLocalHistory()
        updateCache(local)
    }

    private suspend fun updateCache(draws: List<HistoricalDraw>) {
        if (draws.isEmpty()) return
        cacheMutex.withLock {
            draws.forEach { historyCache[it.contestNumber] = it }
        }
    }
}