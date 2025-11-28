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
private const val SUM_DISTRIBUTION_GROUPING = 10
private const val DEFAULT_GROUPING = 1
private const val ALL_CONTESTS_WINDOW = 0

@Singleton
class StatisticsAnalyzer @Inject constructor(
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    // LruCache é thread-safe e lida com a remoção de itens antigos automaticamente
    private val analysisCache = LruCache<String, StatisticsReport>(CACHE_SIZE)

    suspend fun analyze(draws: List<HistoricalDraw>, timeWindow: Int = ALL_CONTESTS_WINDOW): StatisticsReport =
        withContext(defaultDispatcher) {
            if (draws.isEmpty()) return@withContext StatisticsReport()

            val drawsToAnalyze = if (timeWindow > ALL_CONTESTS_WINDOW) draws.take(timeWindow) else draws
            if (drawsToAnalyze.isEmpty()) return@withContext StatisticsReport()

            val cacheKey = generateCacheKey(drawsToAnalyze)
            analysisCache.get(cacheKey)?.let { return@withContext it }

            // Cálculos sequenciais são eficientes o suficiente aqui, evitando overhead de contexto
            val distributions = calculateAllDistributions(drawsToAnalyze)
            
            val report = StatisticsReport(
                mostFrequentNumbers = calculateMostFrequent(drawsToAnalyze),
                mostOverdueNumbers = calculateMostOverdue(drawsToAnalyze),
                evenDistribution = distributions.evenDistribution,
                primeDistribution = distributions.primeDistribution,
                frameDistribution = distributions.frameDistribution,
                portraitDistribution = distributions.portraitDistribution,
                fibonacciDistribution = distributions.fibonacciDistribution,
                multiplesOf3Distribution = distributions.multiplesOf3Distribution,
                sumDistribution = distributions.sumDistribution,
                averageSum = calculateAverageSum(drawsToAnalyze),
                totalDrawsAnalyzed = drawsToAnalyze.size,
                analysisDate = System.currentTimeMillis()
            )

            analysisCache.put(cacheKey, report)
            report
        }

    private fun calculateMostFrequent(draws: List<HistoricalDraw>): List<NumberFrequency> {
        val frequencies = IntArray(LotofacilConstants.MAX_NUMBER + 1)
        draws.flatMap { it.numbers }.forEach { number ->
             if (number in LotofacilConstants.VALID_NUMBER_RANGE) frequencies[number]++
        }

        return (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER)
            .map { NumberFrequency(it, frequencies[it]) }
            .sortedByDescending { it.frequency }
            .take(TOP_NUMBERS_COUNT)
    }

    private fun calculateMostOverdue(draws: List<HistoricalDraw>): List<NumberFrequency> {
        if (draws.isEmpty()) return emptyList()

        val lastContestNumber = draws.first().contestNumber
        // Mapa: Número -> Último concurso visto
        val lastSeenMap = mutableMapOf<Int, Int>()
        
        // Itera reverso (do mais antigo pro mais novo) ou verifica existência
        // Como 'draws' está ordenado decrescente, o primeiro encontro é o mais recente
        for (draw in draws) {
            for (number in draw.numbers) {
                lastSeenMap.putIfAbsent(number, draw.contestNumber)
            }
            if (lastSeenMap.size == LotofacilConstants.MAX_NUMBER) break
        }

        return (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER)
            .map { number ->
                val lastSeen = lastSeenMap[number] ?: 0
                NumberFrequency(number, if(lastSeen == 0) -1 else lastContestNumber - lastSeen)
            }
            .sortedByDescending { it.frequency }
            .take(TOP_NUMBERS_COUNT)
    }

    private fun calculateAllDistributions(draws: List<HistoricalDraw>): DistributionResults {
        return DistributionResults(
            evenDistribution = calculateDistribution(draws) { it.evens },
            primeDistribution = calculateDistribution(draws) { it.primes },
            frameDistribution = calculateDistribution(draws) { it.frame },
            portraitDistribution = calculateDistribution(draws) { it.portrait },
            fibonacciDistribution = calculateDistribution(draws) { it.fibonacci },
            multiplesOf3Distribution = calculateDistribution(draws) { it.multiplesOf3 },
            sumDistribution = calculateDistribution(draws, SUM_DISTRIBUTION_GROUPING) { it.sum }
        )
    }

    private inline fun calculateDistribution(
        draws: List<HistoricalDraw>,
        grouping: Int = DEFAULT_GROUPING,
        crossinline valueExtractor: (HistoricalDraw) -> Int
    ): Map<Int, Int> {
        return draws.groupingBy { draw ->
            (valueExtractor(draw) / grouping) * grouping
        }.eachCount()
    }

    private fun calculateAverageSum(draws: List<HistoricalDraw>): Float {
        if (draws.isEmpty()) return 0f
        // mapToInt evita autoboxing no sum()
        return draws.stream().mapToInt { it.sum }.average().orElse(0.0).toFloat()
    }

    private fun generateCacheKey(draws: List<HistoricalDraw>): String {
        val first = draws.firstOrNull()?.contestNumber ?: 0
        val last = draws.lastOrNull()?.contestNumber ?: 0
        return "$first-$last-${draws.size}"
    }

    private data class DistributionResults(
        val evenDistribution: Map<Int, Int>,
        val primeDistribution: Map<Int, Int>,
        val frameDistribution: Map<Int, Int>,
        val portraitDistribution: Map<Int, Int>,
        val fibonacciDistribution: Map<Int, Int>,
        val multiplesOf3Distribution: Map<Int, Int>,
        val sumDistribution: Map<Int, Int>
    )
}