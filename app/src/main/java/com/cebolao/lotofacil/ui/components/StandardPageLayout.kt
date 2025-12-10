package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
    
    // Bottom padding logic: Scaffold padding usually includes nav bar if present,
    // but if we want extra space for visuals (Fab, BottomBar), we add it here.
    val bottomPadding = if (addBottomSpace) {
        scaffoldPadding.calculateBottomPadding() + Dimen.BottomContentPadding
    } else {
        scaffoldPadding.calculateBottomPadding() + Dimen.SpacingMedium
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding() + Dimen.SpacingMedium,
            start = scaffoldPadding.calculateStartPadding(layoutDirection) + Dimen.ScreenPadding, // Add global horizontal padding
            end = scaffoldPadding.calculateEndPadding(layoutDirection) + Dimen.ScreenPadding,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
        content = content
    )
}