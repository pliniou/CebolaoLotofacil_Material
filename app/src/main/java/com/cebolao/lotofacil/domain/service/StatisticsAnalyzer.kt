package com.cebolao.lotofacil.domain.service

import android.util.LruCache
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.StatisticsReport
import com.cebolao.lotofacil.data.model.NumberFrequency
import com.cebolao.lotofacil.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TOP_NUMBERS_COUNT = 5
private const val CACHE_SIZE = 50
private const val MAX_SUM_BUCKET = 300 // Soma máx teórica é 25*15 ~ mas 300 cobre folgado
private const val MAX_BUCKET_SIZE = 30 // Para histogramas pequenos (pares, primos, etc)

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    // Cache simples: Chave = "count_firstContest_lastContest"
    private val analysisCache = LruCache<String, StatisticsReport>(CACHE_SIZE)

    suspend fun analyze(draws: List<HistoricalDraw>, timeWindow: Int = 0): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            val drawsToAnalyze = if (timeWindow > 0) draws.take(timeWindow) else draws
            if (drawsToAnalyze.isEmpty()) return@withContext StatisticsReport()

            val cacheKey = "${drawsToAnalyze.size}_${drawsToAnalyze.first().contestNumber}_${drawsToAnalyze.last().contestNumber}"
            
            analysisCache.get(cacheKey)?.let { return@withContext it }

            val report = calculateStatistics(drawsToAnalyze)
            analysisCache.put(cacheKey, report)
            report
        }

    private fun calculateStatistics(draws: List<HistoricalDraw>): StatisticsReport {
        // Arrays primitivos para evitar alocação de Integer em Maps
        val frequencies = IntArray(LotofacilConstants.MAX_NUMBER + 1)
        val lastSeen = IntArray(LotofacilConstants.MAX_NUMBER + 1) { -1 }
        
        // Histogramas fixos (Range conhecido)
        val evensDist = IntArray(16) // 0..15
        val primesDist = IntArray(16)
        val frameDist = IntArray(17) // 0..16
        val portraitDist = IntArray(16)
        val fibonacciDist = IntArray(16)
        val mult3Dist = IntArray(16)
        
        // Soma agrupa por dezenas, range aprox 120..270. Array de 300 cobre tudo.
        val sumDist = IntArray(MAX_SUM_BUCKET)
        
        var sumAccumulator = 0L
        val latestContest = draws.first().contestNumber

        // Single pass loop
        for (draw in draws) {
            // Stats por número
            for (num in draw.numbers) {
                frequencies[num]++
                if (lastSeen[num] == -1) {
                    lastSeen[num] = draw.contestNumber
                }
            }
            
            // Stats globais do jogo (incremento direto em array)
            safeInc(evensDist, draw.evens)
            safeInc(primesDist, draw.primes)
            safeInc(frameDist, draw.frame)
            safeInc(portraitDist, draw.portrait)
            safeInc(fibonacciDist, draw.fibonacci)
            safeInc(mult3Dist, draw.multiplesOf3)
            
            val sumBucket = (draw.sum / 10) * 10
            safeInc(sumDist, sumBucket)
            
            sumAccumulator += draw.sum
        }

        // Converte arrays para listas/mapas exigidos pelo objeto de retorno (apenas no final)
        val mostFrequent = createFrequencyList(frequencies, null, TOP_NUMBERS_COUNT)
        val mostOverdue = createFrequencyList(null, lastSeen, TOP_NUMBERS_COUNT, latestContest)

        return StatisticsReport(
            mostFrequentNumbers = mostFrequent,
            mostOverdueNumbers = mostOverdue,
            evenDistribution = evensDist.toMapDist(),
            primeDistribution = primesDist.toMapDist(),
            frameDistribution = frameDist.toMapDist(),
            portraitDistribution = portraitDist.toMapDist(),
            fibonacciDistribution = fibonacciDist.toMapDist(),
            multiplesOf3Distribution = mult3Dist.toMapDist(),
            sumDistribution = sumDist.toMapDist(), // Filter 0s handled in extension
            averageSum = sumAccumulator.toFloat() / draws.size,
            totalDrawsAnalyzed = draws.size
        )
    }

    private fun safeInc(array: IntArray, index: Int) {
        if (index in array.indices) array[index]++
    }

    private fun IntArray.toMapDist(): Map<Int, Int> {
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
                val value = if (counts != null) {
                    counts[num]
                } else {
                    if (lastSeen!![num] == -1) -1 else currentContest - lastSeen[num]
                }
                NumberFrequency(num, value)
            }
            .sortedByDescending { it.frequency }
            .take(limit)
    }
}