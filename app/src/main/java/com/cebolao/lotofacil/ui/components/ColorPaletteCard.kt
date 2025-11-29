package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.darkColorSchemeFor
import com.cebolao.lotofacil.ui.theme.lightColorSchemeFor

@Composable
fun ColorPaletteCard(
    currentPalette: AccentPalette,
    onPaletteChange: (AccentPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    val palettes = AccentPalette.entries
    val isDarkTheme = isSystemInDarkTheme()

    // Otimização: Memoização dos esquemas de cores
    val previewColorSchemes = remember(isDarkTheme, palettes) {
        palettes.associateWith {
            if (isDarkTheme) darkColorSchemeFor(it) else lightColorSchemeFor(it)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Text(
            "Cores de Destaque", 
            style = MaterialTheme.typography.titleSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
            contentPadding = PaddingValues(end = Dimen.ScreenPadding) // Espaço para scroll
        ) {
            items(palettes) { palette ->
                val colorScheme = previewColorSchemes[palette]!!
                PalettePreviewCard(
                    colorScheme = colorScheme,
                    name = palette.paletteName,
                    isSelected = currentPalette == palette,
                    onClick = { onPaletteChange(palette) }
                )
            }
        }
    }
}

@Composable
private fun PalettePreviewCard(
    colorScheme: ColorScheme,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else Color.Transparent,
        animationSpec = tween(AppConfig.Animation.SHORT_DURATION),
        label = "border"
    )

    Card(
        modifier = modifier
            .width(Dimen.PaletteCardWidth)
            .height(Dimen.PaletteCardHeight)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(if (isSelected) Dimen.Border.Thick else Dimen.Border.Hairline, borderColor),
        elevation = CardDefaults.cardElevation(if (isSelected) Dimen.Elevation.Medium else Dimen.Elevation.Low)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimen.MediumPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding)
            ) {
                ColorSwatch(colorScheme.primary, modifier = Modifier.weight(1f))
                ColorSwatch(colorScheme.tertiary, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = colorScheme.onSurface,
                    maxLines = 1
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.general_selected),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(20.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
            .border(
                width = Dimen.Border.Hairline,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            )
    )
}