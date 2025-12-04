package com.cebolao.lotofacil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.domain.usecase.ObserveAppConfigUseCase
import com.cebolao.lotofacil.domain.usecase.UpdateAppConfigUseCase
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(val isReady: Boolean = false)
data class StartDestinationState(val destination: String = Screen.Onboarding.route, val isLoading: Boolean = true)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeAppConfigUseCase: ObserveAppConfigUseCase,
    private val updateAppConfigUseCase: UpdateAppConfigUseCase
) : ViewModel() {

    val startDestination = observeAppConfigUseCase.hasCompletedOnboarding
        .map { completed ->
            StartDestinationState(if (completed) Screen.Home.route else Screen.Onboarding.route, false)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), StartDestinationState())

    val uiState = startDestination.map { MainUiState(!it.isLoading) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), MainUiState())

    val themeMode = observeAppConfigUseCase.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), THEME_MODE_LIGHT)

    val accentPalette = observeAppConfigUseCase.accentPalette
        .map { name -> AccentPalette.entries.find { it.name == name } ?: AccentPalette.AZUL }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), AccentPalette.AZUL)

    fun onOnboardingComplete() = viewModelScope.launch { updateAppConfigUseCase.completeOnboarding() }
    fun setThemeMode(mode: String) = viewModelScope.launch { updateAppConfigUseCase.setThemeMode(mode) }
    fun setAccentPalette(palette: AccentPalette) = viewModelScope.launch { updateAppConfigUseCase.setAccentPalette(palette.name) }
}