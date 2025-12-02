package com.cebolao.lotofacil.data

import android.util.Log

private const val TAG = "HistoryParser"
private const val DELIMITER_DASH = '-'
private const val DELIMITER_COMMA = ','

object HistoryParser {
    
    /**
     * Parseia uma linha no formato "1234 - 01,02,03..." de forma otimizada.
     * Evita Regex e split de String para minimizar alocação de memória em loops grandes (parsing de assets).
     */
    fun parseLine(line: String): HistoricalDraw? {
        if (line.isBlank()) return null

        // Encontra o separador do número do concurso
        val dashIndex = line.indexOf(DELIMITER_DASH)
        if (dashIndex == -1) return null

        return try {
            // Extrai número do concurso
            // substring é otimizado no Android (dependendo da versão da JVM, mas geralmente seguro aqui)
            val contestPart = line.take(dashIndex).trim()
            val contestNumber = contestPart.toIntOrNull() ?: return null

            // Extrai números
            val numbersPart = line.substring(dashIndex + 1)
            val numbers = parseNumbersOptimized(numbersPart)

            if (isValidDraw(numbers)) {
                HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(TAG, "Skipping invalid draw data for contest $contestNumber: size=${numbers.size}")
                null
            }
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
        // Processa o último número após a última vírgula
        parseAndAddToken(source, currentStart, len, numbers)
        return numbers
    }

    private fun parseAndAddToken(source: String, start: Int, end: Int, destination: MutableSet<Int>) {
        if (start >= end) return
        // trim() aloca nova string, mas é necessário para input sujo.
        // toIntOrNull evita exceções custosas de NumberFormatException
        val token = source.substring(start, end).trim()
        token.toIntOrNull()?.let { destination.add(it) }
    }

    private fun isValidDraw(numbers: Set<Int>): Boolean {
        return numbers.size == LotofacilConstants.GAME_SIZE && 
               numbers.all { it in LotofacilConstants.VALID_NUMBER_RANGE }
    }

    /**
     * Formata um sorteio para persistência ou exibição.
     * Otimizado para evitar String.format em loop se possível.
     */
    fun formatLine(draw: HistoricalDraw): String = buildString {
        append(draw.contestNumber)
        append(" - ")
        
        val sortedNumbers = draw.numbers.sorted()
        for (i in sortedNumbers.indices) {
            val num = sortedNumbers[i]
            if (num < 10) append('0')
            append(num)
            
            if (i < sortedNumbers.size - 1) {
                append(DELIMITER_COMMA)
            }
        }
    }
}