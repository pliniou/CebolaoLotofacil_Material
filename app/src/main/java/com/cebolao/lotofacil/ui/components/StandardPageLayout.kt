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

/**
 * Standard layout for scrollable pages.
 * Integrates with Scaffold padding and handles WindowInsets.
 */
@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    addBottomSpace: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val navBarsPadding = WindowInsets.navigationBars.asPaddingValues()
    
    // Bottom padding logic: Scaffold padding usually includes nav bar if present,
    // but if we want extra space for visuals, we add it here.
    val bottomPadding = if (addBottomSpace) {
        scaffoldPadding.calculateBottomPadding() + Dimen.BottomContentPadding
    } else {
        scaffoldPadding.calculateBottomPadding() + Dimen.MediumPadding
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding() + Dimen.MediumPadding,
            start = scaffoldPadding.calculateStartPadding(layoutDirection), // We assume content handles horizontal padding or we add it globally
            end = scaffoldPadding.calculateEndPadding(layoutDirection),
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
        content = content
    )
}