package com.cebolao.lotofacil.ui.theme

object AppConfig {

    object Animation {
        const val SHORT_DURATION = 200
        const val MEDIUM_DURATION = 300
        const val LONG_DURATION = 450
        const val SPLASH_EXIT_DURATION = 500

        const val DELAY_NEXT_DRAW = 0L
        const val DELAY_LAST_DRAW = 75L
        const val DELAY_STATS = 150L
        const val DELAY_CHARTS = 225L
        const val DELAY_EXPLANATION = 300L
        const val DELAY_CHECKER_RESULT = 150L
    }

    object UI {
        const val ANIMATE_ENTRY_OFFSET_DIVISOR = 10
        
        // BarChart
        const val CHART_GRID_LINES = 4
        const val CHART_DASH_INTERVAL = 5f
        const val CHART_LABEL_ROTATION = -45f
        const val CHART_GRID_ALPHA = 0.3f
        
        const val SUM_MIN_RANGE = 120
        const val SUM_MAX_RANGE = 270
        const val SUM_STEP = 10

        // Opções de Interface
        val TIME_WINDOWS = listOf(0, 20, 50, 100, 200, 500)
        val GAME_QUANTITY_OPTIONS = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        // Alphas & Opacities
        const val ALPHA_DISABLED = 0.3f
        const val ALPHA_SELECTED = 0.9f
        const val ALPHA_HIGHLIGHT = 0.2f
        const val ALPHA_BORDER_DEFAULT = 0.25f
        const val STAT_INFO_CHIP_DISABLED_ALPHA = 0.5f
        const val COLOR_SWATCH_BORDER_ALPHA = 0.25f
        const val FILTER_CARD_BORDER_ALPHA = 0.25f
        const val TIME_WINDOW_CHIP_BORDER_ALPHA = 0.25f
        
        const val GRID_COLUMNS = 5
        const val NUMBER_GRID_ITEMS_PER_ROW = 5
        const val BALL_TEXT_FACTOR = 2.8f
        
        const val CHECKER_CHART_SUFFIX_LENGTH = 4
        const val CHECKER_CHART_MIN_MAX_VALUE = 10
        const val FILTER_PANEL_OUTLINE_ALPHA = 0.1f
        const val FILTER_PANEL_PROB_LOW = 0.15f
        const val FILTER_PANEL_PROB_MEDIUM = 0.45f
        const val FILTER_PANEL_TRACK_ALPHA = 0.25f
        const val FILTER_PANEL_CHIP_BG_ALPHA = 0.15f
        const val STATS_PANEL_LOADING_OVERLAY_ALPHA = 0.8f
        const val ONBOARDING_IMAGE_FRACTION = 0.65f
    }
}