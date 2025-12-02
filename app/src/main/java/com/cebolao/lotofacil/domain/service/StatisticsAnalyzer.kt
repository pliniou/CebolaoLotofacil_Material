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

// Tamanhos de Histogramas (Max Value + 1)
private const val HIST_SIZE_EVENS = 16    // 0..15
private const val HIST_SIZE_PRIMES = 16   // 0..15 (teoricamente max 9 primos, mas mantemos margem)
private const val HIST_SIZE_FRAME = 17    // 0..16
private const val HIST_SIZE_PORTRAIT = 16 // 0..15
private const val HIST_SIZE_FIB = 16      // 0..15
private const val HIST_SIZE_MULT3 = 16    // 0..15
private const val HIST_SIZE_SUM = 300     // Soma máx teórica 25*15 + margem

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    // Chave composta para cache: "Qtd_Inicio_Fim"
    private val analysisCache = LruCache<String, StatisticsReport>(CACHE_SIZE)

    suspend fun analyze(draws: List<HistoricalDraw>, timeWindow: Int = 0): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            // Define janela de análise
            val drawsToAnalyze = if (timeWindow > 0) draws.take(timeWindow) else draws
            if (drawsToAnalyze.isEmpty()) return@withContext StatisticsReport()

            val cacheKey = "${drawsToAnalyze.size}_${drawsToAnalyze.first().contestNumber}_${drawsToAnalyze.last().contestNumber}"
            
            synchronized(analysisCache) {
                analysisCache.get(cacheKey)?.let { return@withContext it }
            }

            val report = calculateStatistics(drawsToAnalyze)
            
            synchronized(analysisCache) {
                analysisCache.put(cacheKey, report)
            }
            report
        }

    /**
     * Executa análise em passagem única (Single Pass Loop) para performance.
     * Utiliza IntArrays para evitar alocação excessiva de objetos Integer (Autoboxing).
     */
    private fun calculateStatistics(draws: List<HistoricalDraw>): StatisticsReport {
        // Inicialização de Arrays de Frequência
        val frequencies = IntArray(LotofacilConstants.MAX_NUMBER + 1)
        val lastSeen = IntArray(LotofacilConstants.MAX_NUMBER + 1) { -1 }
        
        // Histogramas
        val evensDist = IntArray(HIST_SIZE_EVENS)
        val primesDist = IntArray(HIST_SIZE_PRIMES)
        val frameDist = IntArray(HIST_SIZE_FRAME)
        val portraitDist = IntArray(HIST_SIZE_PORTRAIT)
        val fibonacciDist = IntArray(HIST_SIZE_FIB)
        val mult3Dist = IntArray(HIST_SIZE_MULT3)
        val sumDist = IntArray(HIST_SIZE_SUM)
        
        var sumAccumulator = 0L
        val latestContest = draws.first().contestNumber

        // Single Pass Loop O(N * 15)
        for (draw in draws) {
            // Stats por número individual
            for (num in draw.numbers) {
                frequencies.safeIncrement(num)
                if (lastSeen[num] == -1) {
                    lastSeen[num] = draw.contestNumber
                }
            }
            
            // Stats globais do concurso
            evensDist.safeIncrement(draw.evens)
            primesDist.safeIncrement(draw.primes)
            frameDist.safeIncrement(draw.frame)
            portraitDist.safeIncrement(draw.portrait)
            fibonacciDist.safeIncrement(draw.fibonacci)
            mult3Dist.safeIncrement(draw.multiplesOf3)
            
            // Bucket da soma (arredonda para dezena mais próxima: 184 -> 180)
            val sumBucket = (draw.sum / 10) * 10
            sumDist.safeIncrement(sumBucket)
            
            sumAccumulator += draw.sum
        }

        // Materialização dos resultados
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

    @Suppress("SameParameterValue")
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