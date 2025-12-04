package com.cebolao.lotofacil.data

import android.util.Log

private const val TAG = "HistoryParser"
private const val DELIMITER_DASH = '-'
private const val DELIMITER_COMMA = ','

object HistoryParser {
    
    fun parseLine(line: String): HistoricalDraw? {
        if (line.isBlank()) return null
        val dashIndex = line.indexOf(DELIMITER_DASH)
        if (dashIndex == -1) return null

        return try {
            val contestNumber = line.take(dashIndex).trim().toIntOrNull() ?: return null
            val numbers = parseNumbersOptimized(line.substring(dashIndex + 1))

            if (isValidDraw(numbers)) HistoricalDraw(contestNumber, numbers) else null
        } catch (e: Exception) {
            Log.w(TAG, "Parse error on line: $line", e)
            null
        }
    }

    private fun parseNumbersOptimized(source: String): Set<Int> {
        val numbers = HashSet<Int>(LotofacilConstants.GAME_SIZE)
        var currentStart = 0
        val len = source.length
        
        for (i in 0 until len) {
            if (source[i] == DELIMITER_COMMA) {
                parseAndAddToken(source, currentStart, i, numbers)
                currentStart = i + 1
            }
        }
        parseAndAddToken(source, currentStart, len, numbers)
        return numbers
    }

    private fun parseAndAddToken(source: String, start: Int, end: Int, destination: MutableSet<Int>) {
        if (start >= end) return
        source.substring(start, end).trim().toIntOrNull()?.let { destination.add(it) }
    }

    private fun isValidDraw(numbers: Set<Int>): Boolean {
        return numbers.size == LotofacilConstants.GAME_SIZE && 
               numbers.all { it in LotofacilConstants.NUMBER_RANGE }
    }

    fun formatLine(draw: HistoricalDraw): String = buildString {
        append(draw.contestNumber).append(" - ")
        draw.numbers.sorted().joinTo(this, ",") { 
            if (it < 10) "0$it" else it.toString() 
        }
    }
}