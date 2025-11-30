package com.cebolao.lotofacil.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Utilitários centralizados para formatação.
 */
object Formatters {
    
    // NumberFormat não é thread-safe, então usamos uma função factory ou ThreadLocal se fosse crítico.
    // Para chamadas simples, criar a instância é barato o suficiente no Android moderno.
    private fun getCurrencyInstance(): NumberFormat = 
        NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))

    fun formatCurrency(value: Double): String = getCurrencyInstance().format(value)
    
    fun formatCurrency(value: BigDecimal): String = getCurrencyInstance().format(value)
}

/**
 * Retorna uma instância memorizada para uso em Composables (evita recriação na recomposição).
 */
@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    return remember {
        NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))
    }
}