package com.cebolao.lotofacil.data

import android.util.Log
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

private const val TAG = "HistoryParser"

object HistoryParser {
    // Regex pré-compilada para performance. Captura: Grupo 1 (Concurso), Grupo 2 (Números)
    private val LINE_REGEX = """^\s*(\d+)\s*-\s*([\d,\s]+)$""".toRegex()
    
    fun parseLine(line: String): HistoricalDraw? {
        if (line.isBlank()) return null
        
        val match = LINE_REGEX.find(line.trim()) ?: return null
        val (contestStr, numbersStr) = match.destructured

        return try {
            val contestNumber = contestStr.toInt()
            val numbers = numbersStr.split(',')
                .map { it.trim().toInt() }
                .filter { it in LotofacilConstants.VALID_NUMBER_RANGE }
                .toSet()

            if (numbers.size == LotofacilConstants.GAME_SIZE) {
                HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(TAG, "Invalid number count for contest $contestNumber: ${numbers.size}")
                null
            }
        } catch (e: NumberFormatException) {
            Log.w(TAG, "Parse error on line: $line", e)
            null
        }
    }

    fun formatLine(draw: HistoricalDraw): String {
        // Usa StringBuilder internamente via joinToString para eficiência
        val numbersStr = draw.numbers.sorted().joinToString(",") { DEFAULT_NUMBER_FORMAT.format(it) }
        return "${draw.contestNumber} - $numbersStr"
    }
}