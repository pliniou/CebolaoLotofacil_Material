package com.cebolao.lotofacil

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private companion object {
        const val SPLASH_INTERPOLATOR_TENSION = 1.5f
        const val SPLASH_ICON_EXIT_SCALE = 1.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        splash.setOnExitAnimationListener { splashScreenViewProvider ->
            setupSplashScreenExitAnimation(splashScreenViewProvider)
        }

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val accentPaletteName by mainViewModel.accentPalette.collectAsStateWithLifecycle()

            // Correção: Fallback para AZUL se o nome não for encontrado (ou DEFAULT se preferir)
            val accentPalette = remember(accentPaletteName) {
                AccentPalette.entries.find { it.name == accentPaletteName } ?: AccentPalette.AZUL
            }

            splash.setKeepOnScreenCondition { !uiState.isReady }

            val useDarkTheme = themeMode == THEME_MODE_DARK

            // Correção: Removido 'dynamicColor' que causava erro de compilação
            CebolaoLotofacilTheme(
                darkTheme = useDarkTheme,
                accentPalette = accentPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isReady) {
                        MainScreen(mainViewModel = mainViewModel)
                    }
                }
            }
        }
    }

    private fun setupSplashScreenExitAnimation(splashScreenViewProvider: SplashScreenViewProvider) {
        val splashView = splashScreenViewProvider.view
        val iconView = splashScreenViewProvider.iconView
        val exitDuration = AppConfig.Animation.SPLASH_EXIT_DURATION.toLong()

        ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f).apply {
            interpolator = AnticipateInterpolator(SPLASH_INTERPOLATOR_TENSION)
            duration = exitDuration
            doOnEnd { splashScreenViewProvider.remove() }
        }.start()

        ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, SPLASH_ICON_EXIT_SCALE).apply {
            duration = exitDuration
        }.start()
        ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, SPLASH_ICON_EXIT_SCALE).apply {
            duration = exitDuration
        }.start()
    }
}