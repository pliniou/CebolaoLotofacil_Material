package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    SectionCard(
        modifier = modifier,
        title = stringResource(R.string.about_theme_title),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SectionSpacing)
        ) {
            ThemeModeSelector(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange
            )

            ColorPaletteCard(
                currentPalette = currentPalette,
                onPaletteChange = onPaletteChange
            )
        }
    }
}

@Composable
private fun ThemeModeSelector(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        ThemeModeButton(
            selected = currentTheme == THEME_MODE_LIGHT,
            icon = Icons.Filled.LightMode,
            label = stringResource(R.string.about_theme_light),
            onClick = { onThemeChange(THEME_MODE_LIGHT) },
            modifier = Modifier.weight(1f)
        )
        ThemeModeButton(
            selected = currentTheme == THEME_MODE_DARK,
            icon = Icons.Filled.DarkMode,
            label = stringResource(R.string.about_theme_dark),
            onClick = { onThemeChange(THEME_MODE_DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeModeButton(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "themeModeContainer"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "themeModeContent"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "themeModeBorder"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(Dimen.ActionButtonHeight),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = Dimen.Elevation.None,
        shadowElevation = Dimen.Elevation.None,
        border = BorderStroke(Dimen.Border.Hairline, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                Dimen.SmallPadding,
                Alignment.CenterHorizontally
            )
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
