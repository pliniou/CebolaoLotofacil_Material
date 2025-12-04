package com.cebolao.lotofacil.data

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(
    @param:StringRes val titleRes: Int,
    val fullRange: ClosedFloatingPointRange<Float>,
    val defaultRange: ClosedFloatingPointRange<Float>,
    val historicalSuccessRate: Float
) {
    SOMA_DEZENAS(
        titleRes = R.string.filter_soma_title,
        fullRange = 120f..270f,
        defaultRange = 170f..220f,
        historicalSuccessRate = 0.62f
    ),

    PARES(
        titleRes = R.string.filter_pares_title,
        fullRange = 0f..12f,
        defaultRange = 6f..9f,
        historicalSuccessRate = 0.68f
    ),

    PRIMOS(
        titleRes = R.string.filter_primos_title,
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        historicalSuccessRate = 0.64f
    ),

    MOLDURA(
        titleRes = R.string.filter_moldura_title,
        fullRange = 0f..15f,
        defaultRange = 8f..11f,
        historicalSuccessRate = 0.66f
    ),

    RETRATO(
        titleRes = R.string.filter_retrato_title,
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        historicalSuccessRate = 0.61f
    ),

    FIBONACCI(
        titleRes = R.string.filter_fibonacci_title,
        fullRange = 0f..7f,
        defaultRange = 3f..5f,
        historicalSuccessRate = 0.58f
    ),

    MULTIPLOS_DE_3(
        titleRes = R.string.filter_multiplos3_title,
        fullRange = 0f..8f,
        defaultRange = 3f..6f,
        historicalSuccessRate = 0.59f
    ),

    REPETIDAS_CONCURSO_ANTERIOR(
        titleRes = R.string.filter_repetidas_title,
        fullRange = 0f..15f,
        defaultRange = 8f..10f,
        historicalSuccessRate = 0.74f
    );
}