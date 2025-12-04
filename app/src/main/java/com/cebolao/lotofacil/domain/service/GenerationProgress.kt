package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.domain.model.LotofacilGame // Corrigido

enum class GenerationStep {
    RANDOM_START,
    HEURISTIC_START,
    RANDOM_FALLBACK
}

enum class GenerationFailureReason {
    NO_HISTORY,
    GENERIC_ERROR
}

sealed interface GenerationProgressType {
    data object Started : GenerationProgressType
    data class Step(val step: GenerationStep) : GenerationProgressType
    data object Attempt : GenerationProgressType
    data class Finished(val games: List<LotofacilGame>) : GenerationProgressType
    data class Failed(val reason: GenerationFailureReason) : GenerationProgressType
}

data class GenerationProgress(
    val current: Int,
    val total: Int,
    val progressType: GenerationProgressType
) {
    companion object {
        fun started(total: Int) =
            GenerationProgress(0, total, GenerationProgressType.Started)

        fun step(step: GenerationStep, current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Step(step))

        fun attempt(current: Int, total: Int) =
            GenerationProgress(current, total, GenerationProgressType.Attempt)

        fun finished(games: List<LotofacilGame>) =
            GenerationProgress(games.size, games.size, GenerationProgressType.Finished(games))

        fun failed(reason: GenerationFailureReason) =
            GenerationProgress(0, 0, GenerationProgressType.Failed(reason))
    }
}