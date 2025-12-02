package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.CaixaBlue
import com.cebolao.lotofacil.ui.theme.CaixaOrange
import com.cebolao.lotofacil.ui.theme.Dimen
import androidx.core.net.toUri

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(Dimen.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.LargePadding)
        ) {
            // 1. Branding App
            StudioHero()

            // 2. Personalização
            Text(stringResource(R.string.about_appearance), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ThemeSettingsCard(
                currentTheme = currentTheme,
                currentPalette = currentPalette,
                onThemeChange = onThemeChange,
                onPaletteChange = onPaletteChange
            )

            // 3. Link Oficial Caixa
            Text(stringResource(R.string.about_official_source), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OfficialCaixaCard(onClick = {
                val intent = Intent(Intent.ACTION_VIEW,
                    "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx".toUri())
                context.startActivity(intent)
            })

            // 4. Informações Legais
            Text(stringResource(R.string.about_info_section), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutListItem(Icons.Default.Gavel, stringResource(R.string.about_terms)) {}
                AboutListItem(Icons.Default.PrivacyTip, stringResource(R.string.about_privacy_policy)) {}
                AboutListItem(Icons.Default.Info, stringResource(R.string.about_version_format, "1.0.0")) {}
            }
            
            Spacer(Modifier.height(Dimen.LargePadding))
            Text(
                stringResource(R.string.about_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun OfficialCaixaCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CaixaBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Public, 
                contentDescription = null, 
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.about_caixa_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.about_caixa_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = CaixaOrange)
        }
    }
}

@Composable
private fun AboutListItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    }
}