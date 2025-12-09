package com.cebolao.lotofacil.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Modern shapes with consistent rounding
// 4dp min for small interactions, up to 28dp+ for large containers
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),  // Tags, small chips
    small = RoundedCornerShape(8.dp),       // Buttons, Cards (small)
    medium = RoundedCornerShape(12.dp),     // Cards (feature), Dialogs
    large = RoundedCornerShape(16.dp),      // Large Dialogs, Sheets
    extraLarge = RoundedCornerShape(28.dp)  // Large Sheets, Floating Action Buttons
)