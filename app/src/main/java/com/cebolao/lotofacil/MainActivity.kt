package com.cebolao.lotofacil

import android.animation.ObjectAnimator
import android.media.MediaPlayer
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
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.ui.screens.MainScreen
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import com.cebolao.lotofacil.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val SPLASH_ICON_SCALE_TARGET = 0.5f

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        super.onCreate(savedInstanceState)

        // Play splash sound
        playSplashSound()

        setupSplashScreen(splash)

        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val accentPalette by mainViewModel.accentPalette.collectAsStateWithLifecycle()

            splash.setKeepOnScreenCondition { !uiState.isReady }

            CebolaoLotofacilTheme(
                accentPalette = accentPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isReady) {
                        MainScreen()
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
        val duration = AppConfig.Animation.SPLASH_EXIT_DURATION.toLong()
        
        val fadeOut = ObjectAnimator.ofFloat(provider.view, View.ALPHA, 1f, 0f).apply {
            interpolator = AnticipateInterpolator()
            this.duration = duration
            doOnEnd { provider.remove() }
        }
        
        val icon = provider.iconView
        val scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, SPLASH_ICON_SCALE_TARGET)
        val scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, SPLASH_ICON_SCALE_TARGET)
        val fadeIcon = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0f)

        fadeOut.start()
        scaleX.setDuration(duration).start()
        scaleY.setDuration(duration).start()
        fadeIcon.setDuration(duration).start()
    }

    private fun playSplashSound() {
        try {
            val assetFileDescriptor = assets.openFd("sound_splash.mp3")
            val player = MediaPlayer()
            player.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
            assetFileDescriptor.close()
            player.prepare()
            player.start()
            player.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}