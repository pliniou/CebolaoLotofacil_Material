package com.cebolao.lotofacil.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tema principal da aplicação que aplica o Material Design 3.
 *
 * - Usa o esquema de cores escuro definido em [Color.kt] / [AccentColors.kt].
 * - Mantém visual flat dark independentemente do tema do sistema.
 * - Configura cores da barra de status e navegação para combinar com o tema.
 */
@Composable
fun CebolaoLotofacilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Mantido por compatibilidade
    accentPalette: AccentPalette = AccentPalette.AZUL,
    content: @Composable () -> Unit
) {
    // Conforme orientação, apenas Dark theme é suportado.
    val colorScheme = darkColorSchemeFor(accentPalette)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                // Ícones sempre claros sobre fundo escuro.
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
