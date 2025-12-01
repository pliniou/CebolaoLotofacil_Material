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
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        super.onCreate(savedInstanceState)

        setupSplashScreen(splash)

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val accentPaletteName by mainViewModel.accentPalette.collectAsStateWithLifecycle()

            // Keep splash until ViewModel is ready
            splash.setKeepOnScreenCondition { !uiState.isReady }

            val accentPalette = remember(accentPaletteName) {
                AccentPalette.entries.find { it.name == accentPaletteName } ?: AccentPalette.AZUL
            }

            CebolaoLotofacilTheme(
                darkTheme = themeMode == THEME_MODE_DARK,
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

    private fun setupSplashScreen(splash: SplashScreen) {
        splash.setOnExitAnimationListener { provider ->
            animateSplashExit(provider)
        }
    }

    private fun animateSplashExit(provider: SplashScreenViewProvider) {
        val fadeOut = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
            interpolator = AnticipateInterpolator()
            duration = 400L
            doOnEnd { provider.remove() }
        }
        
        // Efeito de zoom-out no ícone
        val icon = provider.iconView
        val scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 0.5f)
        val scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 0.5f)
        val fadeIcon = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0f)

        // Roda tudo junto (simplificado sem AnimatorSet para performance leve)
        fadeOut.start()
        scaleX.setDuration(400L).start()
        scaleY.setDuration(400L).start()
        fadeIcon.setDuration(400L).start()
    }
}