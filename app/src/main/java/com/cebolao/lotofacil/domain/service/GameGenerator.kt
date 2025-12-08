package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.random.Random

private const val BATCH_SIZE = 50
private const val TIMEOUT_MS = 10000L // 10 seconds max per batch attempt to avoid freezing

class GameGenerator @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    fun generate(quantity: Int, filters: List<FilterState>): Flow<GenerationProgress> = flow {
        emit(GenerationProgress.started(quantity))

        val activeFilters = filters.filter { it.isEnabled }
        val generatedGames = LinkedHashSet<LotofacilGame>(quantity)
        
        // Load history for context-aware filters (e.g. repeated numbers)
        val history = historyRepository.getHistory().take(10) // Only need recent for most checks
        val lastDrawNumbers = history.firstOrNull()?.numbers ?: emptySet()

        if (activeFilters.isEmpty()) {
            // Fast path: Pure random
            emit(GenerationProgress.step(GenerationStep.RANDOM_START, 0, quantity))
            while (generatedGames.size < quantity && currentCoroutineContext().isActive) {
                generatedGames.add(generateRandomGame())
                if (generatedGames.size % BATCH_SIZE == 0) {
                    emit(GenerationProgress.attempt(generatedGames.size, quantity))
                }
            }
        } else {
            // Smart path: Recursive Backtracking
            emit(GenerationProgress.step(GenerationStep.HEURISTIC_START, 0, quantity))
            
            val solver = BacktrackingSolver(activeFilters, lastDrawNumbers)
            val startTime = System.currentTimeMillis()
            
            while (generatedGames.size < quantity && currentCoroutineContext().isActive) {
                // Check timeout
                if (System.currentTimeMillis() - startTime > TIMEOUT_MS * (generatedGames.size + 1)) {
                     emit(GenerationProgress.step(GenerationStep.RANDOM_FALLBACK, generatedGames.size, quantity))
                     break // Fallback to random if strict generation takes too long
                }

                val game = solver.findValidGame()
                if (game != null) {
                    if (generatedGames.add(game)) {
                        emit(GenerationProgress.attempt(generatedGames.size, quantity))
                    }
                } else {
                    // Exhausted search space (unlikely with randomization but possible)
                    break 
                }
            }
        }

        // Fill remaining with random if needed (Fallback)
        if (generatedGames.size < quantity && currentCoroutineContext().isActive) {
            val remaining = quantity - generatedGames.size
            /* 
               If we fell back, it means filters were too strict. 
               We generate random games but try to respect loose filters if possible, 
               or just pure random if not. For now, pure random to ensure completion.
            */
            repeat(remaining) {
                generatedGames.add(generateRandomGame())
            }
        }

        if (generatedGames.isNotEmpty()) {
            emit(GenerationProgress.finished(generatedGames.toList()))
        } else {
            emit(GenerationProgress.failed(GenerationFailureReason.GENERIC_ERROR))
        }
    }

    private fun generateRandomGame(): LotofacilGame {
        val numbers = LotofacilConstants.ALL_NUMBERS.shuffled().take(LotofacilConstants.GAME_SIZE).toSet()
        return LotofacilGame(numbers)
    }

    /**
     * Inner class to handle recursive state efficiently.
     */
    private class BacktrackingSolver(
        private val filters: List<FilterState>,
        private val lastDrawNumbers: Set<Int>
    ) {
        // Pre-computed filter bounds for optimizations
        private val sumBounds = filters.find { it.type == FilterType.SOMA_DEZENAS }?.let { 
            it.selectedRange.start.toInt() to it.selectedRange.endInclusive.toInt() 
        }

        // Current state
        private val currentSelection = IntArray(LotofacilConstants.GAME_SIZE)
        private val availableNumbers = LotofacilConstants.ALL_NUMBERS.toMutableList()

        fun findValidGame(): LotofacilGame? {
            availableNumbers.shuffle() // Randomize search order to get different games
            return if (solve(0, 0)) {
                LotofacilGame(currentSelection.toSet())
            } else {
                null
            }
        }

        /**
         * Recursive backtracking function.
         * @param index Current index in the game (0..14)
         * @param currentSum Optimization: current sum of numbers
         */
        private fun solve(index: Int, currentSum: Int): Boolean {
            // Base case: Game complete
            if (index == LotofacilConstants.GAME_SIZE) {
                return validateFinal(currentSelection.toSet())
            }

            // Pruning: Check potential sum validity
            if (sumBounds != null) {
                val remainingCount = LotofacilConstants.GAME_SIZE - index
                val minPossibleSum = currentSum + minSumOfNext(remainingCount)
                val maxPossibleSum = currentSum + maxSumOfNext(remainingCount)
                
                if (minPossibleSum > sumBounds.second || maxPossibleSum < sumBounds.first) {
                    return false
                }
            }

            // Iterate through candidates
            // Clone available to avoid side effects in recursion branches, 
            // but for performance in deep recursion, we usually maintain one state. 
            // Since we need *random* games, simply iterating the shuffled list is enough.
            // But we need to pick from *remaining* available numbers.
            
            // Optimization: Instead of cloning list, we iterate and check if used. 
            // Given n=25, k=15, simple iteration is fast enough.
            
            for (num in availableNumbers) {
                if (isNumberUsed(num, index)) continue

                // Optimistic Local Check (Pruning 2): Order enforcement
                // Enforce strictly increasing order to avoid permutations of same set (1,2 and 2,1)
                if (index > 0 && num <= currentSelection[index - 1]) continue

                currentSelection[index] = num
                
                if (solve(index + 1, currentSum + num)) {
                    return true
                }
                
                // Backtrack
                currentSelection[index] = 0
            }

            return false
        }

        private fun isNumberUsed(num: Int, currentIndex: Int): Boolean {
            for (i in 0 until currentIndex) {
                if (currentSelection[i] == num) return true
            }
            return false
        }

        // Helper for sum pruning
        private fun minSumOfNext(count: Int): Int {
            // Approximation: sum of specific smallest numbers is hard with random access, 
            // but we can use absolute minimums from 1..25 logic?
            // Better: Just use 1..count sum? Too loose. 
            // Use (count * (count + 1)) / 2 ? 
            // Since we enforce increasing order, the next number must be > currentSelection[index-1]
            // Let's rely on loose bounds: 
            val last = if (currentSelection.isNotEmpty() && currentSelection[0] != 0) currentSelection.maxOrNull() ?: 0 else 0
            // Sum of next 'count' integers starting from last+1
            return (count * (last + 1 + last + count)) / 2 
        }

        private fun maxSumOfNext(count: Int): Int {
            // Sum of 'count' largest integers <= 25
            // (25 + 25-count+1) * count / 2
            return (count * (25 + (25 - count + 1))) / 2
        }

        // Final thorough validation against all filters
        private fun validateFinal(numbers: Set<Int>): Boolean {
            val game = LotofacilGame(numbers) // Temp object for validation
            
            // Re-check sum to be sure (redundant but safe)
            // Check all other filters
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
}