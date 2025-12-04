package com.cebolao.lotofacil.data

import androidx.annotation.StringRes
import com.cebolao.lotofacil.R

/**
 * Representa uma estratégia pré-definida de filtros.
 */
data class FilterPreset(
    val id: String,
    @StringRes val labelRes: Int,
    val rules: Map<FilterType, ClosedFloatingPointRange<Float>>
)

object FilterPresets {
    val all = listOf(
        FilterPreset(
            id = "standard",
            labelRes = R.string.preset_standard,
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 180f..210f,
                FilterType.PARES to 7f..8f,
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 8f..10f,
                FilterType.MOLDURA to 9f..10f,
                FilterType.PRIMOS to 4f..6f
            )
        ),
        FilterPreset(
            id = "balanced",
            labelRes = R.string.preset_balanced,
            rules = mapOf(
                FilterType.SOMA_DEZENAS to 170f..220f,
                FilterType.PARES to 6f..9f,
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 8f..10f
            )
        ),
        FilterPreset(
            id = "math",
            labelRes = R.string.preset_math,
            rules = mapOf(
                FilterType.PRIMOS to 5f..6f,
                FilterType.FIBONACCI to 4f..5f,
                FilterType.MULTIPLOS_DE_3 to 4f..5f
            )
        ),
        FilterPreset(
            id = "surprise",
            labelRes = R.string.preset_surprise,
            rules = mapOf(
                FilterType.REPETIDAS_CONCURSO_ANTERIOR to 9f..9f,
                FilterType.PARES to 5f..7f
            )
        )
    )
}