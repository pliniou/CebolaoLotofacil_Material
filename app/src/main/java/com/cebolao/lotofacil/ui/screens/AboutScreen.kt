package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.CaixaBlue
import com.cebolao.lotofacil.ui.theme.CaixaOrange
import com.cebolao.lotofacil.ui.theme.Dimen

// Constants for URLs to improve readability and maintenance
private const val URL_CAIXA = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
private const val URL_TERMS = "https://github.com/pliniou"
private const val URL_PRIVACY = "https://github.com/"

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    val context = LocalContext.current
    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    AppScreen(title = stringResource(R.string.about_title), subtitle = stringResource(R.string.about_subtitle)) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimen.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
        ) {
            StudioHero()
            
            SectionHeader(stringResource(R.string.about_appearance))
            ThemeSettingsCard(currentTheme, currentPalette, onThemeChange, onPaletteChange)

            SectionHeader(stringResource(R.string.about_official_source))
            CaixaCard { openUrl(URL_CAIXA) }

            SectionHeader(stringResource(R.string.about_info_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                AboutItem(icon = Icons.Default.Gavel, text = stringResource(R.string.about_terms)) { openUrl(URL_TERMS) }
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimen.MediumPadding))
                AboutItem(icon = Icons.Default.PrivacyTip, text = stringResource(R.string.about_privacy_policy)) { openUrl(URL_PRIVACY) }
                HorizontalDivider(modifier = Modifier.padding(horizontal = Dimen.MediumPadding))
                AboutItem(icon = Icons.Default.Info, text = stringResource(R.string.about_version_format, "1.0"), isClickable = false) {}
            }
            Spacer(Modifier.height(Dimen.LargePadding))
            FormattedText(
                text = stringResource(R.string.about_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.ScreenPadding)
            )
        }
    }
}

@Composable private fun SectionHeader(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

@Composable private fun CaixaCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CaixaBlue, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dimen.MediumPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(Dimen.LargeIcon))
            Spacer(Modifier.width(Dimen.LargePadding))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.about_caixa_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.about_caixa_desc), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
            }
            Spacer(Modifier.width(Dimen.LargePadding))
            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = stringResource(R.string.open_external_link), tint = CaixaOrange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AboutItem(icon: ImageVector, text: String, isClickable: Boolean = true, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(text, style = MaterialTheme.typography.bodyLarge)
        },
        modifier = if (isClickable) Modifier.clickable(onClick = onClick) else Modifier,
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            if (isClickable) Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(Dimen.SmallIcon))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent))
}