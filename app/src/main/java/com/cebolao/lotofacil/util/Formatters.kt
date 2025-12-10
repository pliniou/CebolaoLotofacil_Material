package com.cebolao.lotofacil.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private const val LOCALE_DATE_FORMAT = "dd/MM/yyyy"
    private val appLocale = Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY)

    fun formatCurrency(value: Number): String {
        return NumberFormat.getCurrencyInstance(appLocale).format(value)
    }

    fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat(LOCALE_DATE_FORMAT, appLocale).format(java.util.Date(timestamp))
    }
    
    fun getLocale(): Locale = appLocale
}

@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    return remember { NumberFormat.getCurrencyInstance(Formatters.getLocale()) }
}