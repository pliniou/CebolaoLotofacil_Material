package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun WelcomeCard(modifier: Modifier = Modifier) {
    val currentDateTime = remember { LocalTime.now() }
    val currentDate = remember { LocalDate.now() }
    
    // Lógica de Saudação
    val greetingRes = when (currentDateTime.hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }
    
    // Lógica de Data (Pt-BR explícito)
    val dateString = remember { 
        val locale = Locale("pt", "BR")
        val fullDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(currentDate)
        val dayFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
        val dayOfWeek = currentDate.format(dayFormatter).replaceFirstChar { it.uppercase() }
        "$fullDate, $dayOfWeek"
    }

    // Frase Motivacional Aleatória
    val quotes = stringArrayResource(R.array.motivational_quotes)
    val randomQuote = remember { quotes.random() }

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimen.CardContentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding),
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                // Saudação
                Text(
                    text = stringResource(R.string.welcome_message_format, stringResource(greetingRes)),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Data
                Text(
                    text = stringResource(R.string.date_format_full, "", dateString).replace(" ,", ""),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                AppDivider(modifier = Modifier.padding(vertical = Dimen.ExtraSmallPadding))

                // Citação
                Text(
                    text = "“$randomQuote”",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}