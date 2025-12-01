package com.cebolao.lotofacil.data

import android.annotation.SuppressLint
import android.util.Log
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

private const val TAG = "HistoryParser"
private const val DELIMITER_DASH = '-'
private const val DELIMITER_COMMA = ','

object HistoryParser {
    
    /**
     * Parseia uma linha no formato "1234 - 01,02,03..." de forma otimizada.
     * Evita Regex e split de String para minimizar alocação em loops grandes.
     */
    fun parseLine(line: String): HistoricalDraw? {
        if (line.isBlank()) return null

        try {
            val dashIndex = line.indexOf(DELIMITER_DASH)
            if (dashIndex == -1) return null

            // Extrai número do concurso
            val contestPart = line.take(dashIndex).trim()
            val contestNumber = contestPart.toIntOrNull() ?: return null

            // Extrai números
            val numbersPart = line.substring(dashIndex + 1)
            val numbers = parseNumbers(numbersPart)

            // Validação de Regra de Negócio
            if (isValidDraw(numbers)) {
                return HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(TAG, "Invalid draw data for contest $contestNumber: size=${numbers.size}")
                return null
            }
        } catch (e: Exception) {
            // Captura genérica mantida pois parsing manual pode lançar IndexOutOfBounds em linhas corrompidas
            Log.w(TAG, "Parse error on line: $line", e)
            return null
        }
    }

    private fun parseNumbers(source: String): Set<Int> {
        val numbers = HashSet<Int>(LotofacilConstants.GAME_SIZE)
        var currentStart = 0
        val len = source.length
        
        for (i in 0 until len) {
            if (source[i] == DELIMITER_COMMA) {
                parseAndAdd(source, currentStart, i, numbers)
                currentStart = i + 1
            }
        }
        // Adiciona o último número
        parseAndAdd(source, currentStart, len, numbers)
        return numbers
    }

    private fun parseAndAdd(source: String, start: Int, end: Int, destination: MutableSet<Int>) {
        if (start >= end) return
        val token = source.substring(start, end).trim()
        if (token.isNotEmpty()) {
            // toIntOrNull evita exceções custosas em loop
            token.toIntOrNull()?.let { destination.add(it) }
        }
    }

    private fun isValidDraw(numbers: Set<Int>): Boolean {
        return numbers.size == LotofacilConstants.GAME_SIZE && 
               numbers.all { it in LotofacilConstants.NUMBER_RANGE }
    }

    @SuppressLint("DefaultLocale")
    fun formatLine(draw: HistoricalDraw): String = buildString {
        append(draw.contestNumber)
        append(" - ")
        draw.numbers.sorted().joinTo(this, separator = ",") { 
            String.format(DEFAULT_NUMBER_FORMAT, it) 
        }
    }
}