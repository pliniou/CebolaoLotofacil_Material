package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.cebolao.lotofacil.data.LotofacilConstants
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val ALL_NUMBERS = (LotofacilConstants.MIN_NUMBER..LotofacilConstants.MAX_NUMBER).toImmutableList()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberGrid(
    selectedNumbers: Set<Int>,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    allNumbers: ImmutableList<Int> = ALL_NUMBERS,
    maxSelection: Int? = null,
    sizeVariant: NumberBallSize = NumberBallSize.Medium,
    ballVariant: NumberBallVariant = NumberBallVariant.Primary,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(Dimen.SpacingXS, Alignment.CenterHorizontally)
) {
    val haptic = LocalHapticFeedback.current
    val isFull = maxSelection != null && selectedNumbers.size >= maxSelection

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(Dimen.SpacingXS),
        maxItemsInEachRow = AppConfig.UI.GRID_COLUMNS
    ) {
        allNumbers.forEach { number ->
            key(number) {
                val isSelected = number in selectedNumbers
                val clickable = !isFull || isSelected
                Box(
                    Modifier.clip(CircleShape).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = clickable
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNumberClick(number)
                    }
                ) {
                    NumberBall(number, Modifier, sizeVariant, isSelected, !clickable, ballVariant)
                }
            }
        }
    }
}