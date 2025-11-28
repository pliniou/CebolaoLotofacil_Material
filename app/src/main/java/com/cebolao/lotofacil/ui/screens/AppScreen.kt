package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    content: @Composable ((PaddingValues) -> Unit)
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            StandardScreenHeader(
                title = title,
                subtitle = subtitle,
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = Dimen.ScreenPadding)) {
                        navigationIcon?.invoke()
                    }
                },
                actions = actions
            )
        },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost
    ) { innerPadding ->
        content(innerPadding)
    }
}