package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Layout padrão recursivo para telas baseadas em lista.
 * Garante consistência de espaçamento e padding em toda a app.
 */
@Composable
fun StandardPageLayout(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + Dimen.MediumPadding, // Reduzido levemente para evitar buracos grandes
            bottom = contentPadding.calculateBottomPadding() + Dimen.BottomBarOffset,
            start = Dimen.ScreenPadding,
            end = Dimen.ScreenPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimen.CardSpacing),
        content = content
    )
}