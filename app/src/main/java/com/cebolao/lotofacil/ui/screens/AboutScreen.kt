package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.InfoListCard
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
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

    // Dialog de Detalhes
    dialogContent?.let { item ->
        InfoDialog(
            onDismissRequest = { dialogContent = null },
            dialogTitle = item.title,
            icon = item.icon, // Ícone contextualizado
            content = item.content
        )
    }

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle),
        navigationIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) }
    ) { innerPadding ->
        StandardPageLayout(scaffoldPadding = innerPadding) {
            
            // Hero Section (Logo e Slogan)
            item { StudioHero() }
            
            // Configurações de Tema
            item {
                ThemeSettingsCard(
                    currentTheme = currentTheme,
                    currentPalette = currentPalette,
                    onThemeChange = onThemeChange,
                    onPaletteChange = onPaletteChange
                )
            }
            
            // Lista de Informações
            items(infoItems.size) { index ->
                val info = infoItems[index]
                InfoListCard(
                    title = info.title,
                    subtitle = info.subtitle,
                    icon = info.icon,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(), // Ripple padrão é ok aqui
                        onClick = { dialogContent = info }
                    )
                )
            }
        }
    }
}

@Composable
private fun rememberInfoItems(): ImmutableList<InfoItem> {
    val context = LocalContext.current
    return remember(context) {
        listOf(
            InfoItem(
                context.getString(R.string.about_purpose_title),
                context.getString(R.string.about_purpose_subtitle),
                Icons.Default.Calculate
            ) { AboutPurposeContent() },
            
            InfoItem(
                context.getString(R.string.about_rules_title),
                context.getString(R.string.about_rules_subtitle),
                Icons.Default.WorkspacePremium
            ) { AboutRulesContent() },
            
            InfoItem(
                context.getString(R.string.about_bolao_title),
                context.getString(R.string.about_bolao_subtitle),
                Icons.Default.Groups
            ) { AboutBolaoContent() },
            
            InfoItem(
                context.getString(R.string.about_privacy_title),
                context.getString(R.string.about_privacy_subtitle),
                Icons.Default.PrivacyTip
            ) { AboutPrivacyContent() },
            
            InfoItem(
                context.getString(R.string.about_legal_title),
                context.getString(R.string.about_legal_subtitle),
                Icons.Default.Gavel
            ) { AboutLegalContent() }
        ).toImmutableList()
    }
}

@Composable private fun AboutPurposeContent() {
    FormattedText(stringResource(R.string.about_purpose_desc_body))
}
@Composable private fun AboutRulesContent() {
    FormattedText(stringResource(R.string.about_rules_item1))
}
@Composable private fun AboutBolaoContent() {
    FormattedText(stringResource(R.string.about_bolao_desc_body))
}
@Composable private fun AboutPrivacyContent() {
    FormattedText(stringResource(R.string.about_privacy_desc_body))
}
@Composable private fun AboutLegalContent() {
    FormattedText(stringResource(R.string.about_legal_footer))
}