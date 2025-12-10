package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    
    // CustomCard Logic
    val bg = if (pinned) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val border = pinned

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = bg,
        hasBorder = border,
        onClick = null
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Header(game.hashCode(), pinned, { onAction(GameCardAction.Pin) })
            
            Spacer(Modifier.height(Dimen.SmallPadding))
            
            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = {}, // Read only
                modifier = Modifier.fillMaxWidth(),
                maxSelection = LotofacilConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (pinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral
            )
            
            Spacer(Modifier.height(Dimen.SmallPadding))
            
            Actions(pinned, { onAction(GameCardAction.Delete) }, { onAction(GameCardAction.Share) }, { onAction(GameCardAction.Check) })
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
        Text(
            text = "Aposta #${hash.absoluteValue.toString().takeLast(4)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(onClick = onPin) {
            Icon(
                imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = null,
                tint = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        horizontalArrangement = Arrangement.End, // Aligned Right
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Context Actions
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
        }
        
        Spacer(Modifier.width(Dimen.SmallPadding))

        // Check Button (Primary Action for this card if needed, or context menu. User said "Delete/Share aligned right... Button Context...").
        // We keep "Check" as a button because it's important.
        if (pinned) {
             Button(onClick = onCheck, contentPadding = ButtonDefaults.ContentPadding) {
                Text(stringResource(R.string.game_card_action_check))
             }
        } else {
             OutlinedButton(onClick = onCheck) {
                Text(stringResource(R.string.game_card_action_check))
             }
        }
    }
}