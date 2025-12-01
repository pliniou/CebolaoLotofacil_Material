package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun ThemeSettingsCard(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)) {
            // Seção de Tema (Dia/Noite)
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                Text(
                    text = stringResource(R.string.about_theme_title),
                    style = MaterialTheme.typography.titleMedium
                )
                ThemeModeSelector(currentTheme, onThemeChange)
            }

            AppDivider()

            // Seção de Cores (Paletas Dinâmicas)
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
                Text(
                    text = stringResource(R.string.about_personalization_title),
                    style = MaterialTheme.typography.titleMedium
                )
                PaletteSelector(currentPalette, onPaletteChange)
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(currentTheme: String, onThemeChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
    ) {
        ThemeModeButton(
            selected = currentTheme == THEME_MODE_LIGHT,
            icon = Icons.Default.LightMode,
            label = stringResource(R.string.about_theme_light),
            onClick = { onThemeChange(THEME_MODE_LIGHT) },
            modifier = Modifier.weight(1f)
        )
        ThemeModeButton(
            selected = currentTheme == THEME_MODE_DARK,
            icon = Icons.Default.DarkMode,
            label = stringResource(R.string.about_theme_dark),
            onClick = { onThemeChange(THEME_MODE_DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeModeButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "modeContainer"
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "modeContent"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(Dimen.LargeButtonHeight),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(Dimen.SmallPadding))
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = contentColor)
        }
    }
}

@Composable
private fun PaletteSelector(currentPalette: AccentPalette, onPaletteChange: (AccentPalette) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(AccentPalette.entries) { palette ->
            PaletteSwatch(
                palette = palette,
                isSelected = currentPalette == palette,
                onClick = { onPaletteChange(palette) }
            )
        }
    }
}

@Composable
private fun PaletteSwatch(
    palette: AccentPalette,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size by animateDpAsState(if (isSelected) 56.dp else 48.dp, label = "swatchSize")
    
    // Cor do check depende da luminância da seed
    val checkColor = if (palette == AccentPalette.AMARELO || palette == AccentPalette.VERDE) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(palette.seed)
            .border(
                width = if (isSelected) 3.dp else 0.dp, 
                color = MaterialTheme.colorScheme.onSurface, 
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = checkColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}