package com.cebolao.lotofacil.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cebolao.lotofacil.util.CHECKER_ARG_SEPARATOR

@Stable
sealed class Screen(
    val route: String,
    val title: String? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    val baseRoute: String get() = route.substringBefore('?')

    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home", "Início", Icons.Filled.Home, Icons.Outlined.Home)
    data object Filters : Screen("filters", "Geração", Icons.Filled.Tune, Icons.Outlined.Tune)
    data object GeneratedGames : Screen("generated_games", "Jogos", Icons.AutoMirrored.Filled.ListAlt, Icons.AutoMirrored.Outlined.ListAlt)
    data object About : Screen("about", "Sobre", Icons.Filled.Info, Icons.Outlined.Info)

    data object Checker : Screen(
        route = "checker?numbers={numbers}",
        title = "Conferir",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    ) {
        const val ARG_NUMBERS = "numbers"
        val arguments = listOf(navArgument(ARG_NUMBERS) { type = NavType.StringType; nullable = true; defaultValue = null })
        fun createRoute(numbers: Set<Int>) = "checker?$ARG_NUMBERS=${numbers.joinToString(CHECKER_ARG_SEPARATOR.toString())}"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Filters, Screen.GeneratedGames, Screen.Checker, Screen.About)

// Extension util para navegação type-safe
fun NavController.navigateToChecker(numbers: Set<Int>) {
    navigate(Screen.Checker.createRoute(numbers))
}