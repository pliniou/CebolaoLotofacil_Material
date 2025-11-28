package com.cebolao.lotofacil.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Refatorado: Fontes reduzidas para melhorar a densidade da informação.
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp, // Era 26.sp
        lineHeight = 32.sp, // Era 34.sp
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp, // Era 24.sp
        lineHeight = 30.sp, // Era 32.sp
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp, // Era 22.sp
        lineHeight = 28.sp, // Era 30.sp
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp, // Era 20.sp
        lineHeight = 27.sp, // Era 28.sp
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // Era 17.sp
        lineHeight = 24.sp, // Era 25.sp
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // Era 15.sp
        lineHeight = 22.sp, // Era 23.sp
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Gabarito,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp, // Era 18.sp
        lineHeight = 25.sp, // Era 26.sp
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp, // Era 15.sp
        lineHeight = 20.sp, // Era 22.sp
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // Era 13.sp
        lineHeight = 16.sp, // Era 18.sp
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // Era 15.sp
        lineHeight = 20.sp, // Era 22.sp
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // Era 13.sp
        lineHeight = 16.sp, // Era 18.sp
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp, // Era 11.sp
        lineHeight = 14.sp, // Era 15.sp
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // Era 13.sp
        lineHeight = 16.sp, // Era 18.sp
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp, // Era 11.sp
        lineHeight = 14.sp, // Era 15.sp
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp, // Era 10.sp
        lineHeight = 14.sp, // Era 15.sp
        letterSpacing = 0.5.sp
    )
)