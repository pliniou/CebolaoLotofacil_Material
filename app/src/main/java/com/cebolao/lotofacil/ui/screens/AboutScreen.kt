package com.cebolao.lotofacil.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.SectionHeader
import com.cebolao.lotofacil.ui.components.StandardPageLayout
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.CaixaBlue
import com.cebolao.lotofacil.ui.theme.CaixaOrange
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.ui.theme.Shapes

// Constants for URLs
private const val URL_CAIXA = "https://loterias.caixa.gov.br/Paginas/Lotofacil.aspx"
private const val URL_TERMS = "https://google.com"
private const val URL_PRIVACY = "https://google.com"

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    val context = LocalContext.current
    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        context.startActivity(intent)
    }

    AppScreen(
        title = stringResource(R.string.about_title),
        subtitle = stringResource(R.string.about_subtitle)
    ) { padding ->
        StandardPageLayout(scaffoldPadding = padding) {
            item {
                StudioHero()
            }

            item {
                SectionHeader(stringResource(R.string.about_appearance))
                ThemeSettingsCard(currentTheme, currentPalette, onThemeChange, onPaletteChange)
            }

            item {
                SectionHeader(stringResource(R.string.about_resources_title))
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimen.SpacingShort)
                ) {
                    ProbabilityCard()
                    CaixaCard { openUrl(URL_CAIXA) }
                }
            }

            item {
                SectionHeader(stringResource(R.string.about_legal_title))
                com.cebolao.lotofacil.ui.components.SectionCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        AboutItemRow(
                            Icons.Default.Gavel,
                            stringResource(R.string.about_terms)
                        ) { openUrl(URL_TERMS) }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )
                        AboutItemRow(
                            Icons.Default.PrivacyTip,
                            stringResource(R.string.about_privacy_policy)
                        ) { openUrl(URL_PRIVACY) }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.2f
                            )
                        )
                        AboutItemRow(
                            Icons.Default.Info,
                            stringResource(R.string.about_version_format, "1.0"),
                            isClickable = false
                        ) {}
                    }
                }
            }

            item {
                Spacer(Modifier.height(Dimen.SpacingLarge))
                FormattedText(
                    text = stringResource(R.string.about_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimen.ScreenPadding)
                )
            }
        }
    }
}

@Composable
private fun AboutItemRow(
    icon: ImageVector,
    text: String,
    isClickable: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(Dimen.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimen.IconSmall)
        )
        Spacer(Modifier.width(Dimen.SpacingMedium))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isClickable) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ProbabilityCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(Dimen.SpacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Casino,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimen.IconLarge)
            )
            Spacer(Modifier.width(Dimen.SpacingMedium))
            Column {
                Text(
                    text = stringResource(R.string.about_probabilities_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CaixaCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = CaixaBlue,
            contentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(Dimen.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Public,
                contentDescription = null,
                modifier = Modifier.size(Dimen.IconLarge),
                tint = Color.White
            )
            Spacer(Modifier.width(Dimen.SpacingMedium))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.about_caixa_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.about_caixa_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.9f)
                )
            }
            Spacer(Modifier.width(Dimen.SpacingMedium))
            Icon(
                Icons.AutoMirrored.Filled.Launch,
                contentDescription = stringResource(R.string.open_external_link),
                tint = CaixaOrange
            )
        }
    }
}
