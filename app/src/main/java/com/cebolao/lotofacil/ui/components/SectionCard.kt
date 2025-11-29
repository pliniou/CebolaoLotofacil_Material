package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.Dimen

/**
 * Card base para o app. Implementa o estilo "Modern Surface":
 * - Baixa elevação padrão
 * - Borda sutil para definição
 * - Padding interno generoso
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    // Superfície padrão agora usa a cor Surface (Branca/Cinza escuro) ao invés de SurfaceVariant
    // Isso cria um contraste melhor com o Background (Off-white/Preto)
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = Dimen.Elevation.Low),
    border: BorderStroke? = BorderStroke(
        width = Dimen.Border.Hairline, 
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    ),
    contentSpacing: Dp = Dimen.CardPadding,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(contentSpacing),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            content()
        }
    }
}