package com.cebolao.lotofacil.ui.theme

object AppConfig {

    object Animation {
        const val SHORT_DURATION = 200
        const val MEDIUM_DURATION = 300
        const val LONG_DURATION = 450
        const val SPLASH_EXIT_DURATION = 500

    }

    object UI {

        const val SUM_MIN_RANGE = 120
        const val SUM_MAX_RANGE = 270
        const val SUM_STEP = 10

        // Opções de Interface
        val TIME_WINDOWS = listOf(0, 20, 50, 75, 100, 200, 500)
        val GAME_QUANTITY_OPTIONS = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        // Alphas & Opacities
        const val ALPHA_DISABLED = 0.3f
        const val ALPHA_BORDER_DEFAULT = 0.25f
        const val TIME_WINDOW_CHIP_BORDER_ALPHA = 0.25f
        
        const val GRID_COLUMNS = 5
        const val NUMBER_GRID_ITEMS_PER_ROW = 5

    }
}