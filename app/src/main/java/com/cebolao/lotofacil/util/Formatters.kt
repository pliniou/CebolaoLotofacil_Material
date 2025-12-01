package com.cebolao.lotofacil.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object Formatters {
    
    // Locale constante
    private val appLocale = Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY)

    // Cache Thread-Safe para evitar recriação custosa do NumberFormat
    private val currencyFormatCache = ThreadLocal.withInitial {
        NumberFormat.getCurrencyInstance(appLocale)
    }

    fun formatCurrency(value: Double): String {
        return currencyFormatCache.get()?.format(value) ?: "R$ 0,00"
    }
    
    fun formatCurrency(value: BigDecimal): String {
        return currencyFormatCache.get()?.format(value) ?: "R$ 0,00"
    }
        
    fun getLocale(): Locale = appLocale
}

@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    // Como a UI roda na Main Thread, podemos usar o Formatters.getLocale()
    // Mas para composição, o ideal é lembrar da instância.
    return remember {
        NumberFormat.getCurrencyInstance(Formatters.getLocale())
    }
}