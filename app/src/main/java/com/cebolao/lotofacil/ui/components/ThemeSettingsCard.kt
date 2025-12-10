package com.cebolao.lotofacil.ui.components
import com.cebolao.lotofacil.ui.theme.Shapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
        if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "modeContainer"
    )
    val contentColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "modeContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
        label = "modeBorder"
    )
    val fontWeight by animateFloatAsState(
        targetValue = if (selected) FontWeight.Bold.weight.toFloat() else FontWeight.Normal.weight.toFloat(),
        label = "modeFontWeight"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(Dimen.LargeButtonHeight)
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = label, 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight(fontWeight.toInt())
            )
        }
    }
}
