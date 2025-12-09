package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.domain.model.LotofacilGame
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlin.math.absoluteValue

@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit
) {
    val pinned = game.isPinned
    
    // Refined Style: Sutil e Premium
    // Pinned: Fundo levemente tintado, borda suave.
    // Unpinned: Fundo surface, borda muito fina.
    
    val containerColor = if (pinned) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f) // Muito mais sutil
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderStroke = if (pinned) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) // Hairline
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, 
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .padding(Dimen.CardContentPadding)
                .animateContentSize()
        ) {
            Header(
                hash = game.hashCode(),
                pinned = pinned,
                onPin = { onAction(GameCardAction.Pin) }
            )
            
            Spacer(Modifier.height(Dimen.SectionSpacing))
            
            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = {},
                modifier = Modifier.fillMaxWidth(),
                maxSelection = LotofacilConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                // Se pinned, usamos Secondary, mas talvez seja melhor usar Neutral ou Primary com alpha menor se ainda estiver grosseiro.
                // Vamos manter Secondary mas confiar que o tema cuida das cores.
                ballVariant = if (pinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral 
            )
            
            Spacer(Modifier.height(Dimen.MediumPadding))
            
            Actions(
                pinned = pinned,
                onDelete = { onAction(GameCardAction.Delete) },
                onShare = { onAction(GameCardAction.Share) },
                onCheck = { onAction(GameCardAction.Check) }
            )
        }
    }
}

@Composable
private fun Header(hash: Int, pinned: Boolean, onPin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = if(pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(Dimen.SmallIcon)
            )
            
            Text(
                text = "Aposta #${hash.absoluteValue.toString().takeLast(4)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium, // Reduzido de SemiBold para Medium para leveza
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        IconButton(onClick = onPin) {
            AnimatedContent(targetState = pinned, label = "Pin") { isPinned ->
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun Actions(
    pinned: Boolean,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCheck: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.general_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // Menos agressivo
                )
            }
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.general_share),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Botão de conferir mais limpo
        if (pinned) {
             Button(
                onClick = onCheck,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.SmallIcon)
                )
                Spacer(Modifier.width(Dimen.SmallPadding))
                Text(stringResource(R.string.game_card_action_check))
            }
        } else {
             OutlinedButton(
                onClick = onCheck,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(Dimen.SmallIcon)
                )
                Spacer(Modifier.width(Dimen.SmallPadding))
                Text(stringResource(R.string.game_card_action_check))
            }
        }
    }
}