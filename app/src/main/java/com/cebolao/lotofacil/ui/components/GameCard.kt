package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList

@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit
) {
    val isPinned = game.isPinned
    val colorScheme = MaterialTheme.colorScheme

    val elevation by animateDpAsState(
        targetValue = if (isPinned) 4.dp else 1.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (isPinned) colorScheme.surfaceContainerHigh else colorScheme.surface,
        label = "containerColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isPinned) colorScheme.primary.copy(alpha=0.3f) else colorScheme.outlineVariant.copy(alpha=0.2f),
        label = "borderColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if(isPinned) "Aposta Favorita" else "Aposta Gerada", 
                    style = MaterialTheme.typography.labelSmall,
                    color = if(isPinned) colorScheme.primary else colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (isPinned) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.bounceClick { onAction(GameCardAction.Pin) }
                )
            }

            NumberGrid(
                allNumbers = game.numbers.sorted().toImmutableList(),
                selectedNumbers = game.numbers,
                onNumberClick = {}, 
                maxSelection = game.numbers.size,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (isPinned) NumberBallVariant.Primary else NumberBallVariant.Neutral
            )

            AppDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(GameCardAction.Delete) }) {
                    Icon(Icons.Filled.Delete, "Deletar", tint = colorScheme.error.copy(alpha = 0.8f))
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { onAction(GameCardAction.Share) }) {
                    Icon(Icons.Filled.Share, "Compartilhar", tint = colorScheme.onSurfaceVariant)
                }
                
                FilledIconButton(
                    onClick = { onAction(GameCardAction.Check) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.FactCheck, "Conferir")
                }
            }
        }
    }
}