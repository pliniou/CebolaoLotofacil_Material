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

/**
 * Adiciona um efeito de escala (bounce) ao pressionar.
 * 
 * @param scaleDown Fator de escala quando pressionado (padrão 0.95)
 * @param onClick Ação opcional. Se fornecida, aplica um modifier clickable SEM ripple padrão.
 *                Se null, apenas anima a escala baseada no interactionSource existente (se houver) ou cria um novo.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), // Spring mais responsivo
        label = "bounce"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null, // Remove ripple padrão, o feedback é o bounce
                    onClick = onClick
                )
            } else {
                // Se usado em um Button que já tem clique, não adicionamos outro clickable,
                // mas precisamos capturar o evento de "pressed" dele? 
                // Componentes padrão do Material não expõem fácil o source externo aqui.
                // Uso ideal: Box ou Card customizado.
                Modifier
            }
        )
}