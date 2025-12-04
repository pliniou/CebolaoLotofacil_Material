package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.model.NextDrawInfo
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun NextContestHeroCard(info: NextDrawInfo?) {
    if (info == null) return

    val colorScheme = MaterialTheme.colorScheme

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            colorScheme.primaryContainer.copy(alpha = 0.75f),
            colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) 
    ) {
        Box(modifier = Modifier.background(backgroundBrush)) {
            Column(
                modifier = Modifier
                    .padding(Dimen.CardContentPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
            ) {
                // Header: Badge do concurso
                Surface(
                    color = colorScheme.primary.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = stringResource(R.string.home_next_contest, info.contestNumber),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Corpo: Valor do Prêmio (Hero)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = info.formattedPrize,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.home_prize_estimate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(Dimen.SmallPadding))

                // Footer: Informações Adicionais em Glassmorphism
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = Dimen.MediumPadding, vertical = Dimen.SmallPadding),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoItem(
                        icon = Icons.Default.CalendarToday,
                        label = info.formattedDate,
                        tint = colorScheme.secondary
                    )
                    
                    // Separador vertical
                    Box(modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(colorScheme.outlineVariant.copy(alpha = 0.3f)))

                    InfoItem(
                        icon = Icons.Default.LocalAtm,
                        label = "Final 0/5: ${info.formattedPrizeFinalFive}",
                        tint = colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = tint, 
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}