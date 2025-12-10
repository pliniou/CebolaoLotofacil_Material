package com.cebolao.lotofacil.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tema principal da aplicação que aplica o Material Design 3.
 *
 * - Usa o esquema de cores definido em [Color.kt] para os modos claro e escuro.
 * - Detecta o tema do sistema e escolhe o esquema adequado.
 * - Configura cores da barra de status e navegação para combinar com o tema.
 */
@Composable
fun CebolaoLotofacilTheme(
    // O tema escuro é determinado automaticamente pelo sistema Android.
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Paleta de acentuação opcional (ex.: AZUL, VERMELHO). Mantida para compatibilidade.
    accentPalette: AccentPalette = AccentPalette.AZUL,
    content: @Composable () -> Unit
) {
    // Conforme orientação, apenas Dark theme é suportado.
    val colorScheme = darkColorSchemeFor(accentPalette)

    // Configura cores da barra de status e navegação.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                // Ajusta a legibilidade dos ícones da barra de status/navegação.
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    // Aplica o MaterialTheme com o esquema de cores, tipografia e shapes definidos.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}