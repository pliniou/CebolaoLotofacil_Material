package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.theme.Dimen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsCard(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            stringResource(R.string.about_theme_title), 
            style = MaterialTheme.typography.titleSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            // Light
            SegmentedButton(
                selected = currentTheme == THEME_MODE_LIGHT,
                onClick = { onThemeChange(THEME_MODE_LIGHT) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = { SegmentedButtonDefaults.Icon(active = currentTheme == THEME_MODE_LIGHT) { Icon(Icons.Default.LightMode, null, Modifier.size(16.dp)) } },
                label = { Text(stringResource(R.string.about_theme_light)) }
            )
            // Dark
            SegmentedButton(
                selected = currentTheme == THEME_MODE_DARK,
                onClick = { onThemeChange(THEME_MODE_DARK) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = { SegmentedButtonDefaults.Icon(active = currentTheme == THEME_MODE_DARK) { Icon(Icons.Default.DarkMode, null, Modifier.size(16.dp)) } },
                label = { Text(stringResource(R.string.about_theme_dark)) }
            )
        }
    }
}