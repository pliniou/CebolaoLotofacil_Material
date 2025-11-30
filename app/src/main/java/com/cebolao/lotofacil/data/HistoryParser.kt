package com.cebolao.lotofacil.data

import android.util.Log
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT

private const val TAG = "HistoryParser"
private const val DELIMITER_DASH = '-'
private const val DELIMITER_COMMA = ','

object HistoryParser {
    
    /**
     * Parseia uma linha no formato "1234 - 01,02,03..." de forma otimizada.
     * Evita Regex para performance em loops grandes.
     */
    fun parseLine(line: String): HistoricalDraw? {
        if (line.isBlank()) return null

        try {
            // Encontra o separador " - "
            val dashIndex = line.indexOf(DELIMITER_DASH)
            if (dashIndex == -1) return null

            // Extrai número do concurso
            // trim() pode ser evitado se garantirmos o formato, mas mantemos por segurança
            val contestPart = line.substring(0, dashIndex).trim()
            val contestNumber = contestPart.toIntOrNull() ?: return null

            // Extrai números
            val numbersPart = line.substring(dashIndex + 1)
            
            // Parsing manual da lista de números para evitar alocação de List<String> do split
            val numbers = HashSet<Int>(LotofacilConstants.GAME_SIZE)
            var currentStart = 0
            val len = numbersPart.length
            
            var i = 0
            while (i < len) {
                val c = numbersPart[i]
                if (c == DELIMITER_COMMA) {
                    parseAndAdd(numbersPart, currentStart, i, numbers)
                    currentStart = i + 1
                }
                i++
            }
            // Adiciona o último número
            parseAndAdd(numbersPart, currentStart, len, numbers)

            // Validação
            if (numbers.size == LotofacilConstants.GAME_SIZE && 
                numbers.all { it in LotofacilConstants.VALID_NUMBER_RANGE }) {
                return HistoricalDraw(contestNumber, numbers)
            } else {
                Log.w(TAG, "Invalid draw data for contest $contestNumber: size=${numbers.size}")
                return null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Parse error on line: $line", e)
            return null
        }
    }

    private fun parseAndAdd(source: String, start: Int, end: Int, destination: MutableSet<Int>) {
        if (start >= end) return
        // substring + trim + toInt é mais leve que regex, mas ainda gera string temp.
        // Em ultra-alta performance, usariamos um parser char-by-char, 
        // mas isso aqui já é suficiente para o Android.
        val token = source.substring(start, end).trim()
        if (token.isNotEmpty()) {
            token.toIntOrNull()?.let { destination.add(it) }
        }
    }

    fun formatLine(draw: HistoricalDraw): String {
        return buildString {
            append(draw.contestNumber)
            append(" - ")
            val sorted = draw.numbers.sorted()
            for (i in sorted.indices) {
                if (i > 0) append(DELIMITER_COMMA)
                // Formatação manual ou String.format. 
                // String.format é lento, mas aqui é escrita esporádica, então ok usar helper.
                append(String.format(DEFAULT_NUMBER_FORMAT, sorted[i]))
            }
        }
    }
}