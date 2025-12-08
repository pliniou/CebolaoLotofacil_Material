package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.di.DefaultDispatcher
import com.cebolao.lotofacil.domain.model.NumberFrequency
import com.cebolao.lotofacil.domain.model.StatisticsReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val TOP_NUMBERS_COUNT = 5
private const val CACHE_SIZE = 50

// Histogram sizes
private const val HIST_SIZE_EVENS = 16
private const val HIST_SIZE_PRIMES = 16
private const val HIST_SIZE_FRAME = 17
private const val HIST_SIZE_PORTRAIT = 16
private const val HIST_SIZE_FIB = 16
private const val HIST_SIZE_MULT3 = 16
private const val HIST_SIZE_SUM = 300

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    // Mantendo a correção de LruCache para LinkedHashMap sincronizado
    private val analysisCache = Collections.synchronizedMap(
        object : LinkedHashMap<String, StatisticsReport>(CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, StatisticsReport>?): Boolean {
                return size > CACHE_SIZE
            }
        }
    )

    suspend fun analyze(draws: List<HistoricalDraw>, timeWindow: Int = 0): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            val drawsToAnalyze = if (timeWindow > 0) draws.take(timeWindow) else draws
            if (drawsToAnalyze.isEmpty()) return@withContext StatisticsReport()

            val cacheKey = "${drawsToAnalyze.size}_${drawsToAnalyze.first().contestNumber}_${drawsToAnalyze.last().contestNumber}"
            
            analysisCache[cacheKey]?.let { return@withContext it }

            val report = calculateStatistics(drawsToAnalyze)
            
            analysisCache[cacheKey] = report
            report
        }

    private fun calculateStatistics(draws: List<HistoricalDraw>): StatisticsReport {
        val frequencies = IntArray(LotofacilConstants.MAX_NUMBER + 1)
        val lastSeen = IntArray(LotofacilConstants.MAX_NUMBER + 1) { -1 }
        
        val evensDist = IntArray(HIST_SIZE_EVENS)
        val primesDist = IntArray(HIST_SIZE_PRIMES)
        val frameDist = IntArray(HIST_SIZE_FRAME)
        val portraitDist = IntArray(HIST_SIZE_PORTRAIT)
        val fibonacciDist = IntArray(HIST_SIZE_FIB)
        val mult3Dist = IntArray(HIST_SIZE_MULT3)
        val sumDist = IntArray(HIST_SIZE_SUM)
        
        var sumAccumulator = 0L
        val latestContest = draws.first().contestNumber

        for (draw in draws) {
            for (num in draw.numbers) {
                frequencies.safeIncrement(num)
                if (lastSeen[num] == -1) {
                    lastSeen[num] = draw.contestNumber
                }
            }
            
            evensDist.safeIncrement(draw.evens)
            primesDist.safeIncrement(draw.primes)
            frameDist.safeIncrement(draw.frame)
            portraitDist.safeIncrement(draw.portrait)
            fibonacciDist.safeIncrement(draw.fibonacci)
            mult3Dist.safeIncrement(draw.multiplesOf3)
            
            val sumBucket = (draw.sum / 10) * 10
            sumDist.safeIncrement(sumBucket)
            
            sumAccumulator += draw.sum
        }

        val mostFrequent = createFrequencyList(frequencies, null, TOP_NUMBERS_COUNT)
        val mostOverdue = createFrequencyList(null, lastSeen, TOP_NUMBERS_COUNT, latestContest)

        return StatisticsReport(
            mostFrequentNumbers = mostFrequent,
            mostOverdueNumbers = mostOverdue,
            evenDistribution = evensDist.toNonZeroMap(),
            primeDistribution = primesDist.toNonZeroMap(),
            frameDistribution = frameDist.toNonZeroMap(),
            portraitDistribution = portraitDist.toNonZeroMap(),
            fibonacciDistribution = fibonacciDist.toNonZeroMap(),
            multiplesOf3Distribution = mult3Dist.toNonZeroMap(),
            sumDistribution = sumDist.toNonZeroMap(),
            averageSum = if (draws.isNotEmpty()) sumAccumulator.toFloat() / draws.size else 0f,
            totalDrawsAnalyzed = draws.size
        )
    }

    private fun IntArray.safeIncrement(index: Int) {
        if (index in indices) this[index]++
    }

    private fun IntArray.toNonZeroMap(): Map<Int, Int> {
        val map = HashMap<Int, Int>(this.size)
        for (i in indices) {
            if (this[i] > 0) map[i] = this[i]
        }
        return map
    }

    private fun createFrequencyList(
        counts: IntArray?,
        lastSeen: IntArray?,
        limit: Int,
        currentContest: Int = 0
    ): List<NumberFrequency> {
        return (1..LotofacilConstants.MAX_NUMBER)
            .map { num ->
                val value = counts?.get(num) 
                    ?: (if (lastSeen!![num] == -1) -1 else currentContest - lastSeen[num])
                NumberFrequency(num, value)
            }
            .sortedByDescending { it.frequency }
            .take(limit)
    }
}