package com.cebolao.lotofacil.ui.components
import com.cebolao.lotofacil.ui.theme.Shapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.RestrictivenessCategory
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun FilterStatsPanel(
    activeFilters: List<FilterState>,
    successProbability: Float,
    modifier: Modifier = Modifier
) {
    // Outlined Card para distinÃ§Ã£o semÃ¢ntica (Painel de Info vs. BotÃ£o)
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(Dimen.Border.Thin, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.CardContentPadding)
        ) {
            Text(
                text = stringResource(R.string.filters_analysis_title), 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            FilterRestrictiveness(probability = successProbability)
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            FilterStatistics(activeFilters)
        }
    }
}

@Composable
private fun FilterRestrictiveness(probability: Float) {
    val animatedProbability by animateFloatAsState(
        targetValue = probability,
        animationSpec = tween(AppConfig.Animation.MEDIUM_DURATION),
        label = "probabilityAnimation"
    )

    val colorScheme = MaterialTheme.colorScheme
    val (progressColor, textColor) = when {
        animatedProbability < 0.2f -> colorScheme.error to colorScheme.error
        animatedProbability < 0.5f -> colorScheme.tertiary to colorScheme.tertiary
        else -> colorScheme.primary to colorScheme.primary
    }

    val animatedProgressColor by animateColorAsState(targetValue = progressColor, label = "progressColor")
    val animatedTextColor by animateColorAsState(targetValue = textColor, label = "textColor")

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.filters_success_chance), 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(animatedProbability * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = animatedTextColor
            )
        }
        LinearProgressIndicator(
            progress = { animatedProbability },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.ProgressBarHeight)
                .clip(MaterialTheme.shapes.small),
            color = animatedProgressColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}

@Composable
private fun FilterStatistics(filters: List<FilterState>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        if (filters.isEmpty()) {
            Text(
                stringResource(R.string.filters_no_active_filters),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            filters.forEach { filter ->
                FilterStatRow(filter)
            }
        }
    }
}

@Composable
private fun FilterStatRow(filter: FilterState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(filter.type.titleRes), 
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        RestrictivenessChip(filter.restrictivenessCategory)
    }
}

@Composable
private fun RestrictivenessChip(category: RestrictivenessCategory) {
    val colorScheme = MaterialTheme.colorScheme
    val (containerColor, contentColor) = when (category) {
        RestrictivenessCategory.VERY_TIGHT, RestrictivenessCategory.TIGHT -> colorScheme.errorContainer to colorScheme.onErrorContainer
        RestrictivenessCategory.MODERATE -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
        RestrictivenessCategory.LOOSE, RestrictivenessCategory.VERY_LOOSE -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
        RestrictivenessCategory.DISABLED -> colorScheme.surfaceContainer to colorScheme.onSurfaceVariant
    }

    val textRes = when (category) {
        RestrictivenessCategory.VERY_TIGHT -> R.string.restrictiveness_very_tight
        RestrictivenessCategory.TIGHT -> R.string.restrictiveness_tight
        RestrictivenessCategory.MODERATE -> R.string.restrictiveness_moderate
        RestrictivenessCategory.LOOSE -> R.string.restrictiveness_loose
        RestrictivenessCategory.VERY_LOOSE -> R.string.restrictiveness_very_loose
        RestrictivenessCategory.DISABLED -> R.string.restrictiveness_disabled
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.height(Dimen.MediumIcon),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = Dimen.SmallPadding)) {
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
