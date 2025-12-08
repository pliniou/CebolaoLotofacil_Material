package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    
    val containerColor = if (pinned) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    // Modern card style: clean, elevated, no redundant borders
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (pinned) 2.dp else 1.dp)
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
            
            Spacer(Modifier.height(Dimen.SpacingM))
            
            // Grid of numbers
            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = {},
                modifier = Modifier.fillMaxWidth(),
                maxSelection = LotofacilConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (pinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral
            )
            
            Spacer(Modifier.height(Dimen.SpacingL))
            
            AppDivider(modifier = Modifier.padding(bottom = Dimen.SpacingS))

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
                imageVector = if (pinned) Icons.Default.Star else Icons.Default.Tag,
                contentDescription = null,
                tint = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimen.SmallIcon)
            )
            
            Text(
                text = "Aposta #${hash.absoluteValue.toString().takeLast(4)}",
                style = MaterialTheme.typography.labelLarge,
                color = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (pinned) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        }
        
        IconButton(
            onClick = onPin,
            modifier = Modifier.size(40.dp) // Larger touch target
        ) {
            AnimatedContent(targetState = pinned, label = "PinIconAnimation") { isPinned ->
                Icon(
                    imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) stringResource(R.string.game_card_unpinned) else stringResource(R.string.game_card_pinned),
                    tint = if (isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(Dimen.ActionIconSize)
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
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tertiary Actions
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.general_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
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

        Spacer(Modifier.weight(1f)) // Push check button to end
        
        // Primary Action
        Button(
            onClick = onCheck,
            contentPadding = PaddingValues(horizontal = Dimen.MediumPadding, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(Dimen.SmallIcon)
            )
            
            Spacer(Modifier.width(Dimen.SmallPadding))
            
            Text(
                stringResource(R.string.game_card_action_check),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}