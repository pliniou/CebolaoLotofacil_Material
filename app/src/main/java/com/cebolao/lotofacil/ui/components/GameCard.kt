package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.absoluteValue

@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isPinned = game.isPinned

    // Animação suave de cor de fundo quando fixado
    val containerColor by animateColorAsState(
        targetValue = if (isPinned) colorScheme.secondaryContainer.copy(alpha = 0.15f) else colorScheme.surface,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardBg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isPinned) colorScheme.secondary.copy(alpha = 0.5f) else colorScheme.outlineVariant.copy(alpha = 0.3f),
        label = "cardBorder"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if(isPinned) Icons.Default.Star else Icons.Default.Tag,
                        contentDescription = null,
                        tint = if(isPinned) colorScheme.secondary else colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Aposta #${game.hashCode().absoluteValue.toString().takeLast(4)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if(isPinned) colorScheme.secondary else colorScheme.onSurfaceVariant
                    )
                }
                
                // Pin Button com feedback
                IconButton(
                    onClick = { onAction(GameCardAction.Pin) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) colorScheme.secondary else colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimen.SmallPadding))

            // Números
            NumberGrid(
                allNumbers = game.numbers.sorted().toImmutableList(),
                selectedNumbers = game.numbers,
                onNumberClick = {},
                maxSelection = LotofacilConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (isPinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimen.MediumPadding))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ações secundárias menores e mais sutis
                IconButton(onClick = { onAction(GameCardAction.Delete) }) {
                    Icon(Icons.Default.DeleteOutline, stringResource(R.string.general_delete), tint = colorScheme.error.copy(alpha = 0.7f))
                }
                IconButton(onClick = { onAction(GameCardAction.Share) }) {
                    Icon(Icons.Default.Share, stringResource(R.string.general_share), tint = colorScheme.onSurfaceVariant)
                }
                
                Spacer(Modifier.width(Dimen.SmallPadding))
                
                // Ação Primária destacada
                Button(
                    onClick = { onAction(GameCardAction.Check) },
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isPinned) colorScheme.secondary else colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.game_card_action_check))
                }
            }
        }
    }
}