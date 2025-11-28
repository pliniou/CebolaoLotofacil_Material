package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import androidx.compose.foundation.lazy.items // Import crucial adicionado
import com.cebolao.lotofacil.ui.components.*
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Dimen

private data class InfoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    var dialogContent by remember { mutableStateOf<InfoItem?>(null) }
    val infoItems = rememberInfoItems()

    dialogContent?.let { item ->
        InfoDialog(
            onDismissRequest = { },
            dialogTitle = item.title,
            icon = item.icon
        ) { item.content() }
    }

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle),
        navigationIcon = { Icon(Icons.Default.Info, stringResource(R.string.about_title), tint = MaterialTheme.colorScheme.primary) }
    ) { innerPadding ->
        // Recursividade: StandardPageLayout
        StandardPageLayout(contentPadding = innerPadding) {
            item { StudioHero() }

            item {
                AnimateOnEntry {
                    SectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
                            TitleWithIcon(stringResource(R.string.about_personalization_title), Icons.Default.Palette)
                            ThemeSettingsCard(currentTheme, onThemeChange)
                            ColorPaletteCard(currentPalette, onPaletteChange)
                        }
                    }
                }
            }

            items(infoItems, key = { it.title }) { info ->
                AnimateOnEntry {
                    InfoListCard(
                        title = info.title,
                        subtitle = info.subtitle,
                        icon = info.icon,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(),
                            onClick = { }
                        )
                    )
                }
            }
        }
    }
}

// Conteúdos auxiliares e rememberInfoItems (lógica de conteúdo) permanecem inalterados, pois já estavam extraídos.
// Apenas garantindo que imports necessários estejam presentes.
@Composable
private fun rememberInfoItems(): List<InfoItem> {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember {
        listOf(
            InfoItem(context.getString(R.string.about_purpose_title), context.getString(R.string.about_purpose_subtitle), Icons.Default.Lightbulb) { AboutPurposeContent() },
            InfoItem(context.getString(R.string.about_rules_title), context.getString(R.string.about_rules_subtitle), Icons.Default.Gavel) { AboutRulesContent() },
            InfoItem(context.getString(R.string.about_bolao_title), context.getString(R.string.about_bolao_subtitle), Icons.Default.Group) { AboutBolaoContent() },
            InfoItem(context.getString(R.string.about_privacy_title), context.getString(R.string.about_privacy_subtitle), Icons.Default.Lock) { AboutPrivacyContent() },
            InfoItem(context.getString(R.string.about_legal_title), context.getString(R.string.about_legal_subtitle), Icons.Default.Policy) { AboutLegalContent() }
        )
    }
}

@Composable private fun AboutPurposeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        FormattedText(stringResource(R.string.about_purpose_desc_body))
        InfoPoint(stringResource(R.string.about_purpose_item1_title), stringResource(R.string.about_purpose_item1_desc))
        InfoPoint(stringResource(R.string.about_purpose_item2_title), stringResource(R.string.about_purpose_item2_desc))
        InfoPoint(stringResource(R.string.about_purpose_item3_title), stringResource(R.string.about_purpose_item3_desc))
        FormattedText(stringResource(R.string.about_purpose_desc_footer))
    }
}
@Composable private fun AboutRulesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        InfoPoint("1.", stringResource(R.string.about_rules_item1))
        InfoPoint("2.", stringResource(R.string.about_rules_item2))
        InfoPoint("3.", stringResource(R.string.about_rules_item3))
    }
}
@Composable private fun AboutBolaoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        FormattedText(stringResource(R.string.about_bolao_desc_body))
        FormattedText(stringResource(R.string.about_bolao_desc_footer))
    }
}
@Composable private fun AboutPrivacyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        FormattedText(stringResource(R.string.about_privacy_desc_body))
        InfoPoint("•", stringResource(R.string.about_privacy_item1))
        InfoPoint("•", stringResource(R.string.about_privacy_item2))
        InfoPoint("•", stringResource(R.string.about_privacy_item3))
    }
}
@Composable private fun AboutLegalContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        InfoPoint("•", stringResource(R.string.about_legal_item1))
        InfoPoint("•", stringResource(R.string.about_legal_item2))
        InfoPoint("•", stringResource(R.string.about_legal_item3))
        FormattedText(stringResource(R.string.about_legal_footer))
    }
}