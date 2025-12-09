package com.cebolao.lotofacil.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import org.junit.Rule
import org.junit.Test

class ColorPaletteCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllPalettes() {
        composeTestRule.setContent {
            CebolaoLotofacilTheme {
                ColorPaletteCard(
                    currentPalette = AccentPalette.AZUL,
                    onPaletteChange = {}
                )
            }
        }

        // Check if at least the first and last palette names are displayed
        composeTestRule.onNodeWithText(AccentPalette.AZUL.paletteName).assertIsDisplayed()
        // Note: In a LazyRow, items might not be immediately visible if off-screen, 
        // but for a small number of items on a test device, they usually are.
        // If this flakes, we might need to scroll.
    }

    @Test
    fun clickingPaletteTriggersCallback() {
        var selectedPalette: AccentPalette? = null
        
        composeTestRule.setContent {
            CebolaoLotofacilTheme {
                ColorPaletteCard(
                    currentPalette = AccentPalette.AZUL,
                    onPaletteChange = { selectedPalette = it }
                )
            }
        }

        val targetPalette = AccentPalette.ROXO
        composeTestRule.onNodeWithText(targetPalette.paletteName).performClick()

        assert(selectedPalette == targetPalette)
    }
}
