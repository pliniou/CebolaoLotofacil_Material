package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R

// Fonte principal para títulos e elementos de destaque.
val Gabarito = FontFamily(
    Font(R.font.gabarito_regular, FontWeight.Normal),
    Font(R.font.gabarito_medium, FontWeight.Medium),
    Font(R.font.gabarito_semibold, FontWeight.SemiBold),
    Font(R.font.gabarito_bold, FontWeight.Bold),
    Font(R.font.gabarito_extrabold, FontWeight.ExtraBold),
    Font(R.font.gabarito_black, FontWeight.Black),
)

// Fonte secundária para corpo de texto e labels, otimizada para legibilidade.
val Roboto = FontFamily(
    Font(R.font.gabarito_regular, FontWeight.Normal),
    Font(R.font.gabarito_medium, FontWeight.Medium)
)