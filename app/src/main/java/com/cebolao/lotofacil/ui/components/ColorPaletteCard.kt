package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
    modifier: Modifier = Modifier,
) {
    val palettes = remember { AccentPalette.entries }
    val isDarkTheme = isSystemInDarkTheme()
    val animationSpec: AnimationSpec<Color> = tween(AppConfig.Animation.SHORT_DURATION)

    val previewColorSchemes = remember(isDarkTheme) {
        palettes.map { palette ->
            if (isDarkTheme) darkColorSchemeFor(palette) else lightColorSchemeFor(palette)
        }
    }

    @Composable
    fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .height(16.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color)
                .border(
                    width = Dimen.Border.Hairline,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.extraSmall
                )
        )
    }

    @Composable
    fun PalettePreviewCard(
        palette: AccentPalette,
        colorScheme: ColorScheme,
        isSelected: Boolean,
    ) {
        val borderColor by animateColorAsState(
            targetValue = if (isSelected) colorScheme.primary else Color.Transparent,
            animationSpec = animationSpec,
            label = "border"
        )

        Card(
            onClick = { onPaletteChange(palette) },
            modifier = Modifier
                .width(Dimen.PaletteCardWidth)
                .height(Dimen.PaletteCardHeight),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            border = BorderStroke(
                width = if (isSelected) Dimen.Border.Thick else Dimen.Border.Hairline,
                color = borderColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) Dimen.Elevation.Medium else Dimen.Elevation.Low
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.SmallPadding),
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
                        text = palette.paletteName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.general_selected),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Text(stringResource(R.string.about_personalization_title), style = MaterialTheme.typography.titleMedium)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
            contentPadding = PaddingValues(end = Dimen.ScreenPadding)
        ) {
            items(palettes.size) { index ->
                val palette = palettes[index]
                val colorScheme = previewColorSchemes[index]
                PalettePreviewCard(
                    palette = palette,
                    colorScheme = colorScheme,
                    isSelected = currentPalette == palette,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorPaletteCardPreview() {
    MaterialTheme {
        ColorPaletteCard(currentPalette = AccentPalette.entries.first(), onPaletteChange = {})
    }
}