package com.cebolao.lotofacil.data.datasource

import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.network.ApiService
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import com.cebolao.lotofacil.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryRemoteDataSource"
private const val API_REQUEST_BATCH_SIZE = 50

interface HistoryRemoteDataSource {
    suspend fun getLatestDraw(): LotofacilApiResult?
    suspend fun getDrawsInRange(range: IntRange): List<HistoricalDraw>
}

@Singleton
class HistoryRemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryRemoteDataSource {

    override suspend fun getLatestDraw(): LotofacilApiResult? = withContext(ioDispatcher) {
        runCatching {
            apiService.getLatestResult()
        }.onFailure { e ->
            Log.e(TAG, "Failed to fetch latest draw: ${e.message}", e)
        }.getOrNull()
    }

    override suspend fun getDrawsInRange(range: IntRange): List<HistoricalDraw> = withContext(ioDispatcher) {
        if (range.isEmpty()) {
            return@withContext emptyList()
        }

        coroutineScope {
            range.chunked(API_REQUEST_BATCH_SIZE).flatMap { batch ->
                batch.map { contestNumber ->
                    async {
                        runCatching {
                            apiService.getResultByContest(contestNumber)
                        }.getOrNull()?.let {
                            HistoricalDraw.fromApiResult(it)
                        }
                    }
                }
                .awaitAll()
                .filterNotNull()
            }
        }
    }
}