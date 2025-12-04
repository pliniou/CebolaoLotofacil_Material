package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.domain.model.LotofacilGame // Import Corrigido
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.random.Random

private const val MAX_GENERATION_ATTEMPTS = 250_000
private const val BATCH_SIZE = 200
private const val MIN_HISTORY_FOR_HEURISTIC = 10
private const val HISTORY_LOOKUP_SIZE = 200
private const val MIN_HOT_NUMBERS = 8
private const val MAX_HOT_NUMBERS = 13
private const val HOT_POOL_SIZE = 15

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    fun generate(quantity: Int, filters: List<FilterState>): Flow<GenerationProgress> = flow {
        emit(GenerationProgress.started(quantity))

        val activeFilters = filters.filter { it.isEnabled }

        // Caminho Rápido: Sem filtros
        if (activeFilters.isEmpty()) {
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
            val randomGames = generateRandomGames(quantity)
            emit(GenerationProgress.finished(randomGames))
            return@flow
        }

        // Carregar histórico necessário
        val history = historyRepository.getHistory().take(HISTORY_LOOKUP_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers

        // Validação Heurística
        if (history.size < MIN_HISTORY_FOR_HEURISTIC || lastDrawNumbers == null) {
            if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }) {
                emit(GenerationProgress.failed(GenerationFailureReason.NO_HISTORY))
                return@flow
            }
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
        } else {
            emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))
        }

        val (hotPool, coldPool) = prepareNumberPools(history)
        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)
        var attempts = 0
        var gamesSinceLastEmit = 0

        while (generatedGames.size < quantity && attempts < MAX_GENERATION_ATTEMPTS) {
            if (!currentCoroutineContext().isActive) return@flow

            // CORREÇÃO DE LÓGICA: O repeat executava 200 vezes cegamente.
            // Agora verificamos a quantidade DENTRO do loop para parar exatamente no alvo.
            repeat(BATCH_SIZE) {
                if (generatedGames.size >= quantity) return@repeat

                val candidateGame = createHeuristicCandidate(hotPool, coldPool)
                if (validateGame(candidateGame, activeFilters, lastDrawNumbers ?: emptySet())) {
                    if (generatedGames.add(candidateGame)) {
                        gamesSinceLastEmit++
                    }
                }
            }

            attempts += BATCH_SIZE
            if (gamesSinceLastEmit > 0) {
                emit(GenerationProgress.attempt(generatedGames.size, quantity))
                gamesSinceLastEmit = 0
            }
        }

        // Fallback: Se não conseguiu gerar tudo via heurística
        if (generatedGames.size < quantity) {
            emit(GenerationProgress.step(GenerationStep.RANDOM_FALLBACK, generatedGames.size, quantity))
            val remainingNeeded = quantity - generatedGames.size
            generatedGames.addAll(generateRandomGames(remainingNeeded, generatedGames))
            emit(GenerationProgress.attempt(quantity, quantity))
        }

        if (generatedGames.isEmpty()) {
            emit(GenerationProgress.failed(GenerationFailureReason.GENERIC_ERROR))
        } else {
            emit(GenerationProgress.finished(generatedGames.toList()))
        }
    }

    private fun prepareNumberPools(history: List<com.cebolao.lotofacil.data.HistoricalDraw>): Pair<List<Int>, List<Int>> {
        if (history.isEmpty()) {
            val all = LotofacilConstants.ALL_NUMBERS.shuffled()
            return all.take(HOT_POOL_SIZE) to all.drop(HOT_POOL_SIZE)
        }

        val frequencyMap = history.flatMap { it.numbers }
            .groupingBy { it }
            .eachCount()

        val rankedNumbers = LotofacilConstants.ALL_NUMBERS.sortedByDescending {
            frequencyMap[it] ?: 0
        }

        return rankedNumbers.take(HOT_POOL_SIZE) to rankedNumbers.drop(HOT_POOL_SIZE)
    }

    private fun createHeuristicCandidate(hotPool: List<Int>, coldPool: List<Int>): LotofacilGame {
        val maxHot = minOf(MAX_HOT_NUMBERS, hotPool.size)
        val minHot = minOf(MIN_HOT_NUMBERS, maxHot)

        val hotCount = Random.nextInt(minHot, maxHot + 1)
        val coldCount = LotofacilConstants.GAME_SIZE - hotCount

        val selectedHot = hotPool.shuffled().take(hotCount)
        val selectedCold = coldPool.shuffled().take(coldCount)

        return LotofacilGame((selectedHot + selectedCold).toSet())
    }

    private fun generateRandomGames(quantity: Int, exclude: Set<LotofacilGame> = emptySet()): List<LotofacilGame> {
        val result = mutableSetOf<LotofacilGame>()
        var safety = 0
        val maxTries = quantity * 20

        while (result.size < quantity && safety < maxTries) {
            val numbers = LotofacilConstants.ALL_NUMBERS.shuffled().take(LotofacilConstants.GAME_SIZE).toSet()
            val game = LotofacilGame(numbers)
            if (!exclude.contains(game)) {
                result.add(game)
            }
            safety++
        }
        return result.toList()
    }

    private fun validateGame(game: LotofacilGame, filters: List<FilterState>, lastDrawNumbers: Set<Int>): Boolean {
        return filters.all { filter ->
            val valueToCheck = when (filter.type) {
                FilterType.SOMA_DEZENAS -> game.sum
                FilterType.PARES -> game.evens
                FilterType.PRIMOS -> game.primes
                FilterType.MOLDURA -> game.frame
                FilterType.RETRATO -> game.portrait
                FilterType.FIBONACCI -> game.fibonacci
                FilterType.MULTIPLOS_DE_3 -> game.multiplesOf3
                FilterType.REPETIDAS_CONCURSO_ANTERIOR -> game.repeatedFrom(lastDrawNumbers)
            }
            filter.containsValue(valueToCheck)
        }
    }
}