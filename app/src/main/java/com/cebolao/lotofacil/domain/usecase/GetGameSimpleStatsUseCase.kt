package com.cebolao.lotofacil.domain.usecase

import com.cebolao.lotofacil.data.GameStatisticsProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

/**
 * Caso de uso síncrono para formatar estatísticas básicas de um jogo.
 * Operação puramente em memória (CPU bound, mas muito leve), não requer Flow ou Suspend.
 */
class GetGameSimpleStatsUseCase @Inject constructor() {
    
    operator fun invoke(game: GameStatisticsProvider): ImmutableList<Pair<String, String>> {
        if (game.numbers.isEmpty()) return persistentListOf()

        return listOf(
            "Soma das Dezenas" to game.sum.toString(),
            "Números Pares" to game.evens.toString(),
            "Números Ímpares" to game.odds.toString(),
            "Números Primos" to game.primes.toString(),
            "Sequência Fibonacci" to game.fibonacci.toString(),
            "Na Moldura" to game.frame.toString(),
            "No Retrato (Miolo)" to game.portrait.toString(),
            "Múltiplos de 3" to game.multiplesOf3.toString()
        ).toImmutableList()
    }
}