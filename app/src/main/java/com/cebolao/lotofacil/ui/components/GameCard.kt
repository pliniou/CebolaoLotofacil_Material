package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
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
import com.cebolao.lotofacil.domain.model.LotofacilGame // Import Corrigido
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlin.math.absoluteValue

@Composable
fun GameCard(
    game: LotofacilGame,
    modifier: Modifier = Modifier,
    onAction: (GameCardAction) -> Unit
) {
    val pinned = game.isPinned
    val containerColor by animateColorAsState(if (pinned) MaterialTheme.colorScheme.secondaryContainer.copy(0.15f) else MaterialTheme.colorScheme.surface, spring(), label = "bg")
    val borderColor by animateColorAsState(if (pinned) MaterialTheme.colorScheme.secondary.copy(0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(0.3f), label = "border")

    Card(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor), border = BorderStroke(1.dp, borderColor)) {
        Column(Modifier.padding(Dimen.CardContentPadding)) {
            Header(game.hashCode(), pinned) { onAction(GameCardAction.Pin) }
            Spacer(Modifier.height(Dimen.SmallPadding))
            NumberGrid(game.numbers, {}, Modifier.fillMaxWidth(), maxSelection = LotofacilConstants.GAME_SIZE, sizeVariant = NumberBallSize.Small, ballVariant = if (pinned) NumberBallVariant.Secondary else NumberBallVariant.Neutral)
            Spacer(Modifier.height(Dimen.MediumPadding))
            Actions(pinned, { onAction(GameCardAction.Delete) }, { onAction(GameCardAction.Share) }, { onAction(GameCardAction.Check) })
        }
    }
}

@Composable private fun Header(hash: Int, pinned: Boolean, onPin: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (pinned) Icons.Default.Star else Icons.Default.Tag, null, tint = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Aposta #${hash.absoluteValue.toString().takeLast(4)}", style = MaterialTheme.typography.labelMedium, color = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onPin, Modifier.size(32.dp)) {
            Icon(if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, if (pinned) stringResource(R.string.game_card_unpinned) else stringResource(R.string.game_card_pinned), tint = if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable private fun Actions(pinned: Boolean, onDelete: () -> Unit, onShare: () -> Unit, onCheck: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
        IconButton(onDelete) { Icon(Icons.Default.DeleteOutline, stringResource(R.string.general_delete), tint = MaterialTheme.colorScheme.error.copy(0.7f)) }
        IconButton(onShare) { Icon(Icons.Default.Share, stringResource(R.string.general_share), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(Modifier.width(Dimen.SmallPadding))
        Button(onCheck, Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 16.dp), colors = ButtonDefaults.buttonColors(if (pinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)) {
            Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.game_card_action_check))
        }
    }
}