package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// --- FLAT MODERN SHAPES ---
// Cantos moderadamente arredondados para um visual amigável e profissional.

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),  // Cards e chips
    large = RoundedCornerShape(16.dp),   // Diálogos / folhas maiores
    extraLarge = RoundedCornerShape(24.dp)
)
