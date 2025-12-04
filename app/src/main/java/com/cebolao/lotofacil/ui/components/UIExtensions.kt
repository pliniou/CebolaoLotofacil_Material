package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
) = composed {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) scaleDown else 1f, spring(dampingRatio = 0.6f, stiffness = 400f), label = "bounce")

    this.graphicsLayer { scaleX = scale; scaleY = scale }
        .then(if (onClick != null) Modifier.clickable(interactionSource, null, onClick = onClick) else Modifier)
}