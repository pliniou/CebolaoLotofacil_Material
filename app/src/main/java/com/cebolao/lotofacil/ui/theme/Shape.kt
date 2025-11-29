package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Formas mais orgânicas e modernas (raios maiores)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // Tags, Chips pequenos
    small = RoundedCornerShape(12.dp),      // Botões pequenos, Inputs
    medium = RoundedCornerShape(16.dp),     // Cards internos
    large = RoundedCornerShape(24.dp),      // Cards principais, Dialogs
    extraLarge = RoundedCornerShape(32.dp)  // Bottom Sheets, Floating Action Buttons
)