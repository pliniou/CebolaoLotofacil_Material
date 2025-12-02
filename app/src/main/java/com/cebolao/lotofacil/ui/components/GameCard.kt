package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
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
        Column(modifier = Modifier.padding(Dimen.CardContentPadding)) {
            GameCardHeader(
                gameHash = game.hashCode(),
                isPinned = isPinned,
                onPinClick = { onAction(GameCardAction.Pin) }
            )

            Spacer(modifier = Modifier.height(Dimen.SmallPadding))

            GameCardContent(
                numbers = game.numbers,
                isPinned = isPinned
            )

            Spacer(modifier = Modifier.height(Dimen.MediumPadding))

            GameCardActions(
                isPinned = isPinned,
                onDelete = { onAction(GameCardAction.Delete) },
                onShare = { onAction(GameCardAction.Share) },
                onCheck = { onAction(GameCardAction.Check) }
            )
        }
    }
}

@Composable
private fun GameCardHeader(
    gameHash: Int,
    isPinned: Boolean,
    onPinClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if(isPinned) Icons.Default.Star else Icons.Default.Tag,
                contentDescription = null,
                tint = if(isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Aposta #${gameHash.absoluteValue.toString().takeLast(4)}",
                style = MaterialTheme.typography.labelMedium,
                color = if(isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = onPinClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if(isPinned) stringResource(R.string.game_card_unpinned) else stringResource(R.string.game_card_pinned),
                tint = if (isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GameCardContent(
    numbers: Set<Int>,
    isPinned: Boolean
) {
    NumberGrid(
        allNumbers = numbers.sorted().toImmutableList(),
        selectedNumbers = numbers,
        onNumberClick = {},
        maxSelection = LotofacilConstants.GAME_SIZE,
        sizeVariant = NumberBallSize.Small,
        ballVariant = if (isPinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GameCardActions(
    isPinned: Boolean,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onCheck: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.general_delete),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.general_share),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(Modifier.width(Dimen.SmallPadding))
        
        Button(
            onClick = onCheck,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(36.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.game_card_action_check))
        }
    }
}