package com.cebolao.lotofacil.viewmodels

import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.domain.usecase.ObserveAppConfigUseCase
import com.cebolao.lotofacil.domain.usecase.UpdateAppConfigUseCase
import com.cebolao.lotofacil.ui.theme.AccentPalette
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val observeAppConfigUseCase: ObserveAppConfigUseCase = mockk(relaxed = true)
    private val updateAppConfigUseCase: UpdateAppConfigUseCase = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows
        coEvery { observeAppConfigUseCase.themeMode } returns MutableStateFlow(THEME_MODE_LIGHT)
        coEvery { observeAppConfigUseCase.accentPalette } returns MutableStateFlow(AccentPalette.AZUL.name)
        coEvery { observeAppConfigUseCase.hasCompletedOnboarding } returns MutableStateFlow(true)
        
        viewModel = MainViewModel(observeAppConfigUseCase, updateAppConfigUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is ready after initialization`() = runTest(testDispatcher) {
        // Start collecting the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        
        advanceUntilIdle() 
        
        assertEquals(true, viewModel.uiState.value.isReady)
        job.cancel()
    }

    @Test
    fun `setThemeMode calls usecase`() = runTest(testDispatcher) {
        viewModel.setThemeMode(THEME_MODE_DARK)
        
        advanceUntilIdle()
        
        coVerify { updateAppConfigUseCase.setThemeMode(THEME_MODE_DARK) }
    }

    @Test
    fun `setAccentPalette calls usecase`() = runTest(testDispatcher) {
        val newPalette = AccentPalette.ROXO
        viewModel.setAccentPalette(newPalette)
        
        advanceUntilIdle()
        
        coVerify { updateAppConfigUseCase.setAccentPalette(newPalette.name) }
    }
}
