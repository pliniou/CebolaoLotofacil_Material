package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                Text(
                    text = stringResource(R.string.about_theme_title),
                    style = MaterialTheme.typography.titleMedium
                )
                ThemeModeSelector(currentTheme, onThemeChange)
            }

            AppDivider()

            ColorPaletteCard(currentPalette, onPaletteChange)
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
    icon: ImageVector,
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