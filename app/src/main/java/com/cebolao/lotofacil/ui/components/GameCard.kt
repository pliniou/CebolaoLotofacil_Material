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
    val isPinned = game.isPinned

    val background = if (isPinned) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    CustomCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = background,
        hasBorder = isPinned,
        onClick = null
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            GameCardHeader(
                gameId = game.hashCode(),
                isPinned = isPinned,
                onPinClick = { onAction(GameCardAction.Pin) }
            )

            Spacer(modifier = Modifier.height(Dimen.SmallPadding))

            NumberGrid(
                selectedNumbers = game.numbers,
                onNumberClick = {}, // read-only
                modifier = Modifier.fillMaxWidth(),
                maxSelection = LotofacilConstants.GAME_SIZE,
                sizeVariant = NumberBallSize.Small,
                ballVariant = if (isPinned) {
                    NumberBallVariant.Secondary
                } else {
                    NumberBallVariant.Neutral
                }
            )

            Spacer(modifier = Modifier.height(Dimen.SmallPadding))

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
    gameId: Int,
    isPinned: Boolean,
    onPinClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Aposta #${gameId.absoluteValue.toString().takeLast(4)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onPinClick) {
            Icon(
                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = null,
                tint = if (isPinned) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
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
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.width(Dimen.SmallPadding))

        if (isPinned) {
            Button(onClick = onCheck) {
                Text(text = stringResource(R.string.game_card_action_check))
            }
        } else {
            OutlinedButton(onClick = onCheck) {
                Text(text = stringResource(R.string.game_card_action_check))
            }
        }
    }
}
