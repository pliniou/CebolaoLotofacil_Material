package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.random.Random

private const val MAX_GENERATION_ATTEMPTS = 500_000
private const val BATCH_SIZE = 200
private const val MIN_HISTORY_FOR_HEURISTIC = 10
private const val HISTORY_LOOKUP_SIZE = 200
private const val MIN_HOT_NUMBERS = 8
private const val MAX_HOT_NUMBERS = 13
private const val SAFETY_LOOP_MULTIPLIER = 100
private const val HOT_POOL_SIZE = 15

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    fun generate(quantity: Int, filters: List<FilterState>): Flow<GenerationProgress> = flow {
        emit(GenerationProgress.started(quantity))

        val activeFilters = filters.filter { it.isEnabled }

        // Caminho rápido: Sem filtros, geração puramente aleatória
        if (activeFilters.isEmpty()) {
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
            val randomGames = generateRandomGames(quantity)
            emit(GenerationProgress.finished(randomGames))
            return@flow
        }

        // Carregar histórico
        val history = historyRepository.getHistory().take(HISTORY_LOOKUP_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers

        // Validação de pré-requisitos para heurística
        if (history.size < MIN_HISTORY_FOR_HEURISTIC || lastDrawNumbers == null) {
            if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }) {
                emit(GenerationProgress.failed(GenerationFailureReason.NO_HISTORY))
                return@flow
            }
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
        } else {
            emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))
        }

        // Preparação dos Pools
        val (hotPool, coldPool) = prepareNumberPools(history)
        
        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)
        var attempts = 0
        var gamesSinceLastEmit = 0

        while (generatedGames.size < quantity && attempts < MAX_GENERATION_ATTEMPTS) {
            if (!currentCoroutineContext().isActive) return@flow

            repeat(BATCH_SIZE) {
                // Otimização: Evita shuffled() completo em loop quente
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

            if (generatedGames.size == quantity) break
        }

        // Fallback: Completa com aleatórios se necessário
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

        // Otimização: pickRandomItems é mais eficiente que shuffled().take() repetidamente
        val selectedHot = hotPool.pickRandomItems(hotCount)
        val selectedCold = coldPool.pickRandomItems(coldCount)

        return LotofacilGame((selectedHot + selectedCold).toSet())
    }

    /**
     * Seleciona N itens aleatórios de uma lista sem criar cópia completa para shuffle.
     * Ideal para pools pequenos.
     */
    private fun <T> List<T>.pickRandomItems(count: Int): List<T> {
        if (count >= this.size) return this
        if (count == 0) return emptyList()
        
        // Para listas pequenas, shuffled().take() é aceitável, mas para evitar alocação excessiva
        // em loop, usamos uma amostragem simples.
        // Como a lista fonte é pequena (max 15), shuffled() ainda é rápido, 
        // mas esta implementação evita criar a lista intermediária completa do shuffle.
        return this.asSequence().shuffled(Random).take(count).toList()
    }

    private fun generateRandomGames(
        quantity: Int,
        exclude: Set<LotofacilGame> = emptySet()
    ): List<LotofacilGame> {
        val games = exclude.toMutableSet()
        val targetSize = games.size + quantity
        var safetyCounter = 0
        val maxLoops = quantity * SAFETY_LOOP_MULTIPLIER

        while (games.size < targetSize && safetyCounter < maxLoops) {
            val numbers = LotofacilConstants.ALL_NUMBERS.shuffled().take(LotofacilConstants.GAME_SIZE).toSet()
            games.add(LotofacilGame(numbers))
            safetyCounter++
        }
        return games.toList().takeLast(quantity)
    }

    private fun validateGame(game: LotofacilGame, filters: List<FilterState>, lastDrawNumbers: Set<Int>): Boolean {
        // Fail-fast: retorna false assim que o primeiro filtro falhar
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