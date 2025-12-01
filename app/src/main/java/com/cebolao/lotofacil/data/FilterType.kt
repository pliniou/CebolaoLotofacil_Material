package com.cebolao.lotofacil.data

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R
import kotlinx.serialization.Serializable

@Serializable
enum class FilterType(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val fullRange: ClosedFloatingPointRange<Float>,
    val defaultRange: ClosedFloatingPointRange<Float>,
    val historicalSuccessRate: Float
) {
    SOMA_DEZENAS(
        titleRes = R.string.filter_soma_title,
        descriptionRes = R.string.filter_soma_desc,
        fullRange = 120f..270f,
        defaultRange = 170f..220f,
        historicalSuccessRate = 0.72f
    ),

    PARES(
        titleRes = R.string.filter_pares_title,
        descriptionRes = R.string.filter_pares_desc,
        fullRange = 0f..12f,
        defaultRange = 6f..9f,
        historicalSuccessRate = 0.78f
    ),

    PRIMOS(
        titleRes = R.string.filter_primos_title,
        descriptionRes = R.string.filter_primos_desc,
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        historicalSuccessRate = 0.74f
    ),

    MOLDURA(
        titleRes = R.string.filter_moldura_title,
        descriptionRes = R.string.filter_moldura_desc,
        fullRange = 0f..15f,
        defaultRange = 8f..11f,
        historicalSuccessRate = 0.76f
    ),

    RETRATO(
        titleRes = R.string.filter_retrato_title,
        descriptionRes = R.string.filter_retrato_desc,
        fullRange = 0f..9f,
        defaultRange = 4f..7f,
        historicalSuccessRate = 0.71f
    ),

    FIBONACCI(
        titleRes = R.string.filter_fibonacci_title,
        descriptionRes = R.string.filter_fibonacci_desc,
        fullRange = 0f..7f,
        defaultRange = 3f..5f,
        historicalSuccessRate = 0.68f
    ),

    MULTIPLOS_DE_3(
        titleRes = R.string.filter_multiplos3_title,
        descriptionRes = R.string.filter_multiplos3_desc,
        fullRange = 0f..8f,
        defaultRange = 3f..6f,
        historicalSuccessRate = 0.69f
    ),

    REPETIDAS_CONCURSO_ANTERIOR(
        titleRes = R.string.filter_repetidas_title,
        descriptionRes = R.string.filter_repetidas_desc,
        fullRange = 0f..15f,
        defaultRange = 8f..10f,
        historicalSuccessRate = 0.84f
    );
}

@Serializable
enum class FilterCategory {
    MATHEMATICAL, DISTRIBUTION, POSITIONAL, TEMPORAL
}

data class FilterPreset(
    @param:StringRes val nameRes: Int,
    @param:StringRes val descriptionRes: Int,
    val settings: Map<FilterType, ClosedFloatingPointRange<Float>?>
)

val filterPresets = listOf(
    FilterPreset(
        nameRes = R.string.preset_standard_name,
        descriptionRes = R.string.preset_standard_desc,
        settings = mapOf(
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (8f..10f),
            FilterType.PARES to (7f..9f),
            FilterType.SOMA_DEZENAS to (180f..220f),
            FilterType.MOLDURA to (9f..11f)
        )
    ),
    FilterPreset(
        nameRes = R.string.preset_balanced_name,
        descriptionRes = R.string.preset_balanced_desc,
        settings = mapOf(
            FilterType.SOMA_DEZENAS to (170f..220f),
            FilterType.PARES to (6f..9f),
            FilterType.PRIMOS to (4f..7f),
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (8f..10f)
        )
    ),
    FilterPreset(
        nameRes = R.string.preset_math_name,
        descriptionRes = R.string.preset_math_desc,
        settings = mapOf(
            FilterType.PRIMOS to (5f..7f),
            FilterType.FIBONACCI to (3f..5f),
            FilterType.MULTIPLOS_DE_3 to (4f..6f),
            FilterType.PARES to (6f..8f)
        )
    ),
    FilterPreset(
        nameRes = R.string.preset_surprise_name,
        descriptionRes = R.string.preset_surprise_desc,
        settings = mapOf(
            FilterType.REPETIDAS_CONCURSO_ANTERIOR to (10f..11f),
            FilterType.PARES to (5f..7f),
            FilterType.PRIMOS to (6f..8f),
            FilterType.FIBONACCI to (5f..6f)
        )
    )
)