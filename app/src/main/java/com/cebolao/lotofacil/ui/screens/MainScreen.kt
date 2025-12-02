package com.cebolao.lotofacil.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cebolao.lotofacil.navigation.Screen
import com.cebolao.lotofacil.navigation.bottomNavItems
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Outfit
import com.cebolao.lotofacil.viewmodels.MainViewModel

@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val accentPaletteName by mainViewModel.accentPalette.collectAsStateWithLifecycle()

    val accentPalette = remember(accentPaletteName) {
        AccentPalette.entries.find { it.name == accentPaletteName } ?: AccentPalette.AZUL
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding ->
        if (!uiState.isReady) {
            LoadingScreen()
        } else {
            NavigationGraph(
                navController = navController,
                startDestination = startDestination.destination,
                modifier = Modifier.padding(innerPadding),
                viewModel = mainViewModel,
                currentTheme = themeMode,
                currentPalette = accentPalette
            )
        }
    }
}

@Composable
private fun AppBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val bottomBarVisible by remember(currentDestination) {
        derivedStateOf { bottomNavItems.any { it.baseRoute == currentDestination?.route?.substringBefore('?') } }
    }

    AnimatedVisibility(
        visible = bottomBarVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route?.substringBefore('?') == screen.baseRoute } == true
                
                NavigationBarItem(
                    selected = selected,
                    alwaysShowLabel = true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        val icon = if (selected) screen.selectedIcon else screen.unselectedIcon
                        icon?.let { Icon(imageVector = it, contentDescription = screen.title) }
                    },
                    label = {
                        screen.title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = Outfit,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
        CircularProgressIndicator() 
    }
}

@Composable
private fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier,
    viewModel: MainViewModel,
    currentTheme: String,
    currentPalette: AccentPalette
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onOnboardingComplete = {
                viewModel.onOnboardingComplete()
                navController.navigate(Screen.Home.route) { 
                    popUpTo(Screen.Onboarding.route) { inclusive = true } 
                }
            })
        }
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Filters.route) { FiltersScreen(navController) }
        composable(Screen.GeneratedGames.route) { GeneratedGamesScreen(navController = navController) }
        composable(route = Screen.Checker.route, arguments = Screen.Checker.arguments) { CheckerScreen() }
        composable(Screen.About.route) {
            AboutScreen(
                currentTheme = currentTheme,
                currentPalette = currentPalette,
                onThemeChange = viewModel::setThemeMode,
                onPaletteChange = viewModel::setAccentPalette
            )
        }
    }
}