package com.cebolao.lotofacil.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.ui.graphics.vector.ImageVector
import com.cebolao.lotofacil.data.FilterType

val FilterType.filterIcon: ImageVector
    get() = when (this) {
        FilterType.SOMA_DEZENAS -> Icons.Default.Calculate
        FilterType.PARES -> Icons.Default.Numbers
        FilterType.PRIMOS -> Icons.Default.Percent
        FilterType.MOLDURA -> Icons.Default.Grid4x4
        FilterType.RETRATO -> Icons.Outlined.ShapeLine
        FilterType.FIBONACCI -> Icons.Default.Timeline
        FilterType.MULTIPLOS_DE_3 -> Icons.Default.Functions
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> Icons.Default.Repeat
    }