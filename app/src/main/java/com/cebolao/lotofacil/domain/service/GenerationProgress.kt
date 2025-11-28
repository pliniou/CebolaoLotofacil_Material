package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.LotofacilGame

/**
 * Representa o tipo de evento de progresso durante a geração de jogos.
 */
sealed class GenerationProgressType {
    data object Started : GenerationProgressType()
    data class Step(val message: String) : GenerationProgressType()
    data object Attempt : GenerationProgressType()
    data class Finished(val games: List<LotofacilGame>) : GenerationProgressType()
    data class Failed(val reason: String) : GenerationProgressType()
}

/**
 * Representa o estado atual do progresso da geração de jogos.
 *
 * @param current O número atual de jogos gerados ou tentativas feitas.
 * @param total A quantidade total de jogos a serem gerados.
 * @param progressType O tipo de evento de progresso.
 */
data class GenerationProgress(
    val current: Int,
    val total: Int,
    val progressType: GenerationProgressType
) {
    companion object {
        fun started(total: Int) = GenerationProgress(0, total, GenerationProgressType.Started)

        fun step(message: String, current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Step(message))

        fun attempt(current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Attempt)

        fun finished(games: List<LotofacilGame>) =
            GenerationProgress(games.size, games.size, GenerationProgressType.Finished(games))

        fun failed(reason: String) =
            GenerationProgress(0, 0, GenerationProgressType.Failed(reason))
    }
}