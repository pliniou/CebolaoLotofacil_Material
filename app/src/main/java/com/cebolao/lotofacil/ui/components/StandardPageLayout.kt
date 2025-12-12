package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Layout padrão para páginas roláveis.
 * Integra com o padding do Scaffold e mantém margens horizontais consistentes.
 */
@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    addBottomSpace: Boolean = true,
    content: LazyListScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current

    val bottomPadding = if (addBottomSpace) {
        scaffoldPadding.calculateBottomPadding() + Dimen.BottomContentPadding
    } else {
        scaffoldPadding.calculateBottomPadding() + Dimen.SpacingMedium
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding() + Dimen.SpacingMedium,
            start = scaffoldPadding.calculateStartPadding(layoutDirection) + Dimen.ScreenPadding,
            end = scaffoldPadding.calculateEndPadding(layoutDirection) + Dimen.ScreenPadding,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing),
        content = content
    )
}
