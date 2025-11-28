package com.cebolao.lotofacil.domain.service

import android.content.Context
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.random.Random

private const val MAX_GENERATION_ATTEMPTS = 500_000
private const val HEURISTIC_CANDIDATES_PER_BATCH = 100
private const val MIN_HISTORY_FOR_HEURISTIC = 50
private const val HISTORY_LOOKUP_SIZE = 200

class GameGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val historyRepository: HistoryRepository
) {
    fun generate(quantity: Int, filters: List<FilterState>): Flow<GenerationProgress> = flow {
        emit(GenerationProgress.started(quantity))

        val activeFilters = filters.filter { it.isEnabled }
        
        // Caminho rápido: Geração aleatória se não houver filtros
        if (activeFilters.isEmpty()) {
            emit(GenerationProgress.step(context.getString(R.string.game_generator_random_start), 0, quantity))
            val randomGames = generateRandomGames(quantity)
            emit(GenerationProgress.finished(randomGames))
            return@flow
        }

        // Carregar histórico
        val history = historyRepository.getHistory().take(HISTORY_LOOKUP_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers

        if (history.size < MIN_HISTORY_FOR_HEURISTIC || lastDrawNumbers == null) {
            emit(GenerationProgress.failed(context.getString(R.string.game_generator_failure_no_history)))
            return@flow
        }

        emit(GenerationProgress.step(context.getString(R.string.game_generator_heuristic_start), 0, quantity))

        // Prepara frequências para heurística
        val numberFrequencies = history.flatMap { it.numbers }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val generatedGames = mutableSetOf<LotofacilGame>()
        var attempts = 0

        // Loop de geração
        while (generatedGames.size < quantity && attempts < MAX_GENERATION_ATTEMPTS) {
            if (!currentCoroutineContext().isActive) return@flow

            val candidates = generateCandidates(numberFrequencies)
            for (candidate in candidates) {
                if (validateGame(candidate, activeFilters, lastDrawNumbers)) {
                    if (generatedGames.add(candidate)) {
                        emit(GenerationProgress.attempt(generatedGames.size, quantity))
                        if (generatedGames.size == quantity) break
                    }
                }
            }
            attempts++
        }

        // Fallback se não conseguiu gerar o suficiente
        if (generatedGames.size < quantity) {
            emit(GenerationProgress.step(context.getString(R.string.game_generator_random_fallback), generatedGames.size, quantity))
            val remainingNeeded = quantity - generatedGames.size
            generatedGames.addAll(generateRandomGames(remainingNeeded, generatedGames))
            emit(GenerationProgress.attempt(quantity, quantity))
        }

        if (generatedGames.isEmpty()) {
            emit(GenerationProgress.failed(context.getString(R.string.game_generator_failure_generic)))
        } else {
            emit(GenerationProgress.finished(generatedGames.toList()))
        }
    }

    private fun generateCandidates(frequencies: List<Int>): List<LotofacilGame> {
        return List(HEURISTIC_CANDIDATES_PER_BATCH) {
            // Heurística: Pega entre 8 e 12 números dos mais frequentes, completa com o resto
            val hotCount = Random.nextInt(8, 13)
            val hotNumbers = frequencies.take(15).shuffled().take(hotCount)
            val coldNumbers = (LotofacilConstants.ALL_NUMBERS - hotNumbers.toSet()).shuffled()
                .take(LotofacilConstants.GAME_SIZE - hotCount)
            
            LotofacilGame((hotNumbers + coldNumbers).toSet())
        }
    }

    private fun generateRandomGames(quantity: Int, exclude: Set<LotofacilGame> = emptySet()): List<LotofacilGame> {
        val games = exclude.toMutableSet()
        val initialSize = games.size
        
        while (games.size < initialSize + quantity) {
            val numbers = LotofacilConstants.ALL_NUMBERS.shuffled().take(LotofacilConstants.GAME_SIZE).toSet()
            games.add(LotofacilGame(numbers))
        }
        return games.toList().takeLast(quantity)
    }

    private fun validateGame(game: LotofacilGame, filters: List<FilterState>, lastDrawNumbers: Set<Int>): Boolean {
        return filters.all { filter ->
            val value = when (filter.type) {
                FilterType.SOMA_DEZENAS -> game.sum
                FilterType.PARES -> game.evens
                FilterType.PRIMOS -> game.primes
                FilterType.MOLDURA -> game.frame
                FilterType.RETRATO -> game.portrait
                FilterType.FIBONACCI -> game.fibonacci
                FilterType.MULTIPLOS_DE_3 -> game.multiplesOf3
                FilterType.REPETIDAS_CONCURSO_ANTERIOR -> game.repeatedFrom(lastDrawNumbers)
            }
            filter.containsValue(value)
        }
    }
}