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
// Define o tamanho fixo do pool quente para garantir sobra para o frio
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

        // Carregar histórico para heurística e filtro de repetidas
        val history = historyRepository.getHistory().take(HISTORY_LOOKUP_SIZE)
        val lastDrawNumbers = history.firstOrNull()?.numbers

        // Se histórico insuficiente
        if (history.size < MIN_HISTORY_FOR_HEURISTIC || lastDrawNumbers == null) {
            // Se tiver filtro de "Repetidas", falhar pois é impossível calcular
            if (activeFilters.any { it.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR }) {
                emit(GenerationProgress.failed(GenerationFailureReason.NO_HISTORY))
                return@flow
            }
            // Tenta gerar aleatório apenas com filtros matemáticos
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
        } else {
            emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))
        }

        // Prepara pools.
        val (hotPool, coldPool) = if (history.isNotEmpty()) {
            prepareNumberPools(history)
        } else {
            // Fallback seguro se histórico vazio
            val all = LotofacilConstants.ALL_NUMBERS.shuffled()
            all.take(HOT_POOL_SIZE) to all.drop(HOT_POOL_SIZE)
        }

        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)
        var attempts = 0
        var gamesSinceLastEmit = 0

        while (generatedGames.size < quantity && attempts < MAX_GENERATION_ATTEMPTS) {
            if (!currentCoroutineContext().isActive) return@flow

            repeat(BATCH_SIZE) {
                val candidateGame = createHeuristicCandidate(hotPool, coldPool)

                // Valida com filtros ativos
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

        // Fallback: Completa com aleatórios se não atingiu a meta (Timeout ou filtros muito restritos)
        if (generatedGames.size < quantity) {
            emit(GenerationProgress.step(GenerationStep.RANDOM_FALLBACK, generatedGames.size, quantity))
            val remainingNeeded = quantity - generatedGames.size
            // Gera jogos puramente aleatórios para preencher
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
        // Mapa de frequência: Número -> Contagem
        val frequencyMap = history.flatMap { it.numbers }
            .groupingBy { it }
            .eachCount()

        // Ordena TODOS os números (1..25) por frequência (descendente).
        // Números não sorteados (count null) vão para o final (0).
        val rankedNumbers = LotofacilConstants.ALL_NUMBERS.sortedByDescending {
            frequencyMap[it] ?: 0
        }

        // Divide explicitamente em Hot (Top 15) e Cold (Resto 10).
        // Isso garante que sempre existam números suficientes em ambos os pools.
        val hotPool = rankedNumbers.take(HOT_POOL_SIZE)
        val coldPool = rankedNumbers.drop(HOT_POOL_SIZE)

        return hotPool to coldPool
    }

    private fun createHeuristicCandidate(hotPool: List<Int>, coldPool: List<Int>): LotofacilGame {
        // Define quantos números quentes pegar (8 a 13)
        // Garante que não pede mais do que o pool tem (embora HOT_POOL_SIZE=15 > 13)
        val maxHot = minOf(MAX_HOT_NUMBERS, hotPool.size)
        val minHot = minOf(MIN_HOT_NUMBERS, maxHot)

        val hotCount = Random.nextInt(minHot, maxHot + 1)
        val coldCount = LotofacilConstants.GAME_SIZE - hotCount

        // Seleciona aleatoriamente dentro dos pools
        val selectedHot = hotPool.shuffled().take(hotCount)
        val selectedCold = coldPool.shuffled().take(coldCount)

        // A soma dos tamanhos será sempre 15, evitando o IllegalArgumentException
        return LotofacilGame((selectedHot + selectedCold).toSet())
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