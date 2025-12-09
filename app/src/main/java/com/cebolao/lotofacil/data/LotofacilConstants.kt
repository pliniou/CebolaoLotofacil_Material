package com.cebolao.lotofacil.data

import java.math.BigDecimal

object LotofacilConstants {
    const val GAME_SIZE = 15
    const val MIN_NUMBER = 1
    const val MAX_NUMBER = 25
    const val MIN_PRIZE_SCORE = 11
    const val HISTORY_CHECK_SIZE = 10

    val NUMBER_RANGE = MIN_NUMBER..MAX_NUMBER
    val ALL_NUMBERS: List<Int> = NUMBER_RANGE.toList()
    val GAME_COST: BigDecimal = BigDecimal("3.50")
    val PRIMOS: Set<Int> = setOf(2, 3, 5, 7, 11, 13, 17, 19, 23)
    val FIBONACCI: Set<Int> = setOf(1, 2, 3, 5, 8, 13, 21)
    val MOLDURA: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 10, 11, 15, 16, 20, 21, 22, 23, 24, 25)
    val MIOLO: Set<Int> = setOf(7, 8, 9, 12, 13, 14, 17, 18, 19)
    val MULTIPLOS_DE_3: Set<Int> = setOf(3, 6, 9, 12, 15, 18, 21, 24)
}