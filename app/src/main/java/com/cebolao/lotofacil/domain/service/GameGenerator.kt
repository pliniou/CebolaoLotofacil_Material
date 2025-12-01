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
private const val MIN_HISTORY_FOR_HEURISTIC = 50
private const val HISTORY_LOOKUP_SIZE = 200
private const val MIN_HOT_NUMBERS = 8
private const val MAX_HOT_NUMBERS = 13 // Exclusive upper bound logic used in Random

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

        // Carregar histórico para heurística e filtro de repetidas
        val history = historyRepository.getHistory().take(HISTORY_LOOKUP_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers

        if (history.size < MIN_HISTORY_FOR_HEURISTIC || lastDrawNumbers == null) {
            emit(GenerationProgress.failed(GenerationFailureReason.NO_HISTORY))
            return@flow
        }

        emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))

        // Prepara pools de números (Quentes vs Frios)
        val (hotPool, coldPool) = prepareNumberPools(history)

        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)
        var attempts = 0
        var gamesSinceLastEmit = 0

        while (generatedGames.size < quantity && attempts < MAX_GENERATION_ATTEMPTS) {
            if (!currentCoroutineContext().isActive) return@flow

            // Processamento em lote para evitar check de cancelamento excessivo
            repeat(BATCH_SIZE) {
                val candidateGame = createHeuristicCandidate(hotPool, coldPool)
                
                if (validateGame(candidateGame, activeFilters, lastDrawNumbers)) {
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

        // Fallback: Completa com aleatórios se não atingiu a meta (timeout/filtros impossíveis)
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
        val hotNumbers = history.flatMap { it.numbers }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val coldNumbers = (LotofacilConstants.ALL_NUMBERS - hotNumbers.toSet()).toList()
        return hotNumbers to coldNumbers
    }

    private fun createHeuristicCandidate(hotPool: List<Int>, coldPool: List<Int>): LotofacilGame {
        val hotCount = Random.nextInt(MIN_HOT_NUMBERS, MAX_HOT_NUMBERS)
        val coldCount = LotofacilConstants.GAME_SIZE - hotCount
        
        // Shuffled().take() é custoso, mas necessário para aleatoriedade real
        val selectedHot = hotPool.shuffled().take(hotCount)
        val selectedCold = coldPool.shuffled().take(coldCount)
        
        return LotofacilGame((selectedHot + selectedCold).toSet())
    }

    private fun generateRandomGames(quantity: Int, exclude: Set<LotofacilGame> = emptySet()): List<LotofacilGame> {
        val games = exclude.toMutableSet()
        val targetSize = games.size + quantity
        var safetyCounter = 0
        val maxLoops = quantity * 100

        while (games.size < targetSize && safetyCounter < maxLoops) {
            val numbers = LotofacilConstants.ALL_NUMBERS.shuffled().take(LotofacilConstants.GAME_SIZE).toSet()
            games.add(LotofacilGame(numbers))
            safetyCounter++
        }
        return games.toList().takeLast(quantity)
    }

    private fun validateGame(game: LotofacilGame, filters: List<FilterState>, lastDrawNumbers: Set<Int>): Boolean {
        // 'all' é idiomático e performático o suficiente para este contexto (fail-fast)
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