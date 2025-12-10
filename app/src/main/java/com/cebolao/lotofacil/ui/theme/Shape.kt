package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// --- FLAT MODERN SHAPES ---
// Moderately rounded corners for a friendly but professional look.

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp), // Standard Card Shape
    large = RoundedCornerShape(16.dp),  // Dialogs / Large Sheets
    extraLarge = RoundedCornerShape(24.dp)
)