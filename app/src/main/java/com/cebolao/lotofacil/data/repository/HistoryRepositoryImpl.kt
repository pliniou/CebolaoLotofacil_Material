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

    // Cache Volátil: Permite leituras de UI (Main Thread) sem travar no Mutex.
    // A lista é imutável e substituída atomicamente.
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
            // Tenta sync inicial em background
            syncHistory()
        }
    }

    override suspend fun getHistory(): List<HistoricalDraw> {
        // Se vazio, tenta carregar síncrono (caso o init ainda não tenha completado)
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
        // Debounce: Se já estiver sincronizando ou não conseguir o lock, aborta.
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
                // Atualiza cache: Novos + Antigos
                updateCache(newDraws + cachedHistory)
            }
        }
    }

    private fun updateCache(allDraws: List<HistoricalDraw>) {
        // Garante ordenação e unicidade antes de publicar para a variável volátil
        cachedHistory = allDraws
            .distinctBy { it.contestNumber }
            .sortedByDescending { it.contestNumber }
    }
}