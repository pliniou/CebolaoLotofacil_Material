package com.cebolao.lotofacil.domain.usecase

import android.util.Log
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

private const val TAG = "CheckGameUseCase"
private const val RECENT_HITS_WINDOW = 15

class CheckGameUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(gameNumbers: Set<Int>): Flow<Result<CheckResult>> = flow {
        val result = runCatching {
            val history = historyRepository.getHistory()
            if (history.isEmpty()) throw IllegalStateException("History unavailable")
            calculateResult(gameNumbers, history)
        }.onFailure { e ->
            Log.e(TAG, "Analysis failed", e)
        }
        emit(result)
    }.flowOn(defaultDispatcher)

    private fun calculateResult(gameNumbers: Set<Int>, history: List<HistoricalDraw>): CheckResult {
        // Mapeia hits apenas para histórico relevante (onde houve acertos mínimos de prêmio) para contagem
        val hitsPerContest = history.map { draw -> 
            draw.contestNumber to draw.numbers.intersect(gameNumbers).size 
        }

        val scoreCounts = hitsPerContest
            .filter { it.second >= LotofacilConstants.MIN_PRIZE_SCORE }
            .groupingBy { it.second }
            .eachCount()
            .toImmutableMap()

        val lastHit = hitsPerContest.firstOrNull { it.second >= LotofacilConstants.MIN_PRIZE_SCORE }

        val recentHits = hitsPerContest.take(RECENT_HITS_WINDOW)
            .reversed()
            .toImmutableList()

        return CheckResult(
            scoreCounts = scoreCounts,
            lastHitContest = lastHit?.first,
            lastHitScore = lastHit?.second,
            lastCheckedContest = history.firstOrNull()?.contestNumber ?: 0,
            recentHits = recentHits
        )
    }
}