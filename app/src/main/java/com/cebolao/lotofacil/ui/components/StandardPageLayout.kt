package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    // Padding vindo do Scaffold (TopBar)
    scaffoldPadding: PaddingValues = PaddingValues(),
    // Se deve adicionar espaço extra no fim (para BottomBar/FAB)
    addBottomSpace: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding() + Dimen.ExtraLargePadding,
            start = Dimen.ScreenPadding + scaffoldPadding.calculateStartPadding(layoutDirection),
            end = Dimen.ScreenPadding + scaffoldPadding.calculateEndPadding(layoutDirection),
            bottom = if (addBottomSpace) Dimen.BottomContentPadding else navBarsPadding.calculateBottomPadding() + Dimen.ExtraLargePadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
        content = content
    )
}