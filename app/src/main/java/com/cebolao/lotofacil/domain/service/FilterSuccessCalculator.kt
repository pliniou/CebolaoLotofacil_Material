package com.cebolao.lotofacil.domain.service

import com.cebolao.lotofacil.data.FilterState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

@Singleton
class FilterSuccessCalculator @Inject constructor() {

    private companion object {
        const val MIN_RANGE_FACTOR = 0.05f
        const val MIN_PROBABILITY_FLOOR = 0.0001f
        const val MAX_PROBABILITY = 1.0f
    }

    operator fun invoke(activeFilters: List<FilterState>): Float {
        if (activeFilters.isEmpty()) return MAX_PROBABILITY

        val strengths = activeFilters.map { filter ->
            val rangeFactor = max(filter.rangePercentage, MIN_RANGE_FACTOR)
            (filter.type.historicalSuccessRate * rangeFactor).coerceIn(MIN_PROBABILITY_FLOOR, MAX_PROBABILITY)
        }

        val logSum = strengths.sumOf { ln(it.toDouble()) }
        val geometricMean = exp(logSum / strengths.size).toFloat()

        return geometricMean.coerceIn(0f, MAX_PROBABILITY)
    }
}