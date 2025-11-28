package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.repository.THEME_MODE_DARK
import com.cebolao.lotofacil.data.repository.THEME_MODE_LIGHT
import com.cebolao.lotofacil.ui.theme.Dimen

private data class ThemeOption(val key: String, val label: String)

@Composable
private fun rememberThemeOptions(): List<ThemeOption> {
    return listOf(
        ThemeOption(THEME_MODE_LIGHT, stringResource(R.string.about_theme_light)),
        ThemeOption(THEME_MODE_DARK, stringResource(R.string.about_theme_dark)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsCard(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeOptions = rememberThemeOptions()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleWithIcon(
            text = stringResource(R.string.about_theme_title),
            icon = Icons.Default.Palette,
            modifier = Modifier.fillMaxWidth()
        )

        // CORRIGIDO: O nome correto é 'SingleChoiceSegmentedButtonRow'
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    modifier = Modifier.weight(1f),
                    // CORRIGIDO: Parâmetro para 'SingleChoice' é 'selected'
                    selected = currentTheme == option.key,
                    // CORRIGIDO: Parâmetro para 'SingleChoice' é 'onClick'
                    onClick = { onThemeChange(option.key) },
                    // A 'shape' agora será resolvida corretamente dentro do scope
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                    label = { Text(option.label) }
                )
            }
        }
    }
}