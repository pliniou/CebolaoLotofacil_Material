package com.cebolao.lotofacil.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cebolao.lotofacil.ui.theme.CebolaoLotofacilTheme
import org.junit.Rule
import org.junit.Test

class NumberBallTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersNumberCorrectly() {
        val number = 15
        composeTestRule.setContent {
            CebolaoLotofacilTheme {
                NumberBall(number = number)
            }
        }

        composeTestRule.onNodeWithText("15").assertIsDisplayed()
    }
    
    // Additional tests for colors/variants could be added here checking semantics or verifying bitmap capture (screenshot tests)
    // For now, confirming it renders the text is a good baseline.
}
