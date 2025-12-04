package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.*

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    val context = LocalContext.current
    AppScreen(title = stringResource(R.string.about_title), subtitle = stringResource(R.string.about_subtitle)) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimen.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
        ) {
            StudioHero()
            
            SectionHeader(stringResource(R.string.about_appearance))
            ThemeSettingsCard(currentTheme, currentPalette, onThemeChange, onPaletteChange)

            SectionHeader(stringResource(R.string.about_official_source))
            CaixaCard { context.startActivity(Intent(Intent.ACTION_VIEW, "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx".toUri())) }

            SectionHeader(stringResource(R.string.about_info_section))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutItem(Icons.Default.Gavel, stringResource(R.string.about_terms)) {}
                AboutItem(Icons.Default.PrivacyTip, stringResource(R.string.about_privacy_policy)) {}
                AboutItem(Icons.Default.Info, stringResource(R.string.about_version_format, "1.0.0")) {}
            }

            Spacer(Modifier.height(Dimen.LargePadding))
            Text(stringResource(R.string.about_disclaimer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable private fun SectionHeader(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

@Composable private fun CaixaCard(onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = CaixaBlue), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Public, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.about_caixa_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(stringResource(R.string.about_caixa_desc), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = CaixaOrange)
        }
    }
}

@Composable private fun AboutItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f), modifier = Modifier.size(16.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.2f))
    }
}