package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        // Atualizado: Level1 -> Low
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimen.Elevation.Low)),
        // Atualizado: Level1 -> Low
        elevation = CardDefaults.cardElevation(Dimen.Elevation.Low)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)
        ) {
            Text(stringResource(R.string.filters_analysis_title), style = MaterialTheme.typography.titleLarge)
            FilterRestrictiveness(probability = successProbability)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = AppConfig.UI.ALPHA_BORDER_DEFAULT))
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

    val (progressColor, textColor) = when {
        animatedProbability < 0.2f -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        animatedProbability < 0.5f -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
    }

    val animatedProgressColor by animateColorAsState(targetValue = progressColor, label = "progressColor")
    val animatedTextColor by animateColorAsState(targetValue = textColor, label = "textColor")

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.filters_success_chance), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${(animatedProbability * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
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
            trackColor = animatedProgressColor.copy(alpha = AppConfig.UI.ALPHA_BORDER_DEFAULT)
        )
    }
}

@Composable
private fun FilterStatistics(filters: List<FilterState>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        if (filters.isEmpty()) {
            Text(
                stringResource(R.string.filters_no_active_filters),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Text(filter.type.title, style = MaterialTheme.typography.bodyMedium)
        RestrictivenessChip(filter.restrictivenessCategory)
    }
}

@Composable
private fun RestrictivenessChip(category: RestrictivenessCategory) {
    val color = when (category) {
        RestrictivenessCategory.VERY_TIGHT, RestrictivenessCategory.TIGHT -> MaterialTheme.colorScheme.error
        RestrictivenessCategory.MODERATE -> MaterialTheme.colorScheme.tertiary
        RestrictivenessCategory.LOOSE, RestrictivenessCategory.VERY_LOOSE -> MaterialTheme.colorScheme.primary
        RestrictivenessCategory.DISABLED -> MaterialTheme.colorScheme.outline
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
        color = color.copy(alpha = AppConfig.UI.ALPHA_DISABLED),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = Dimen.SmallPadding, vertical = Dimen.ExtraSmallPadding)
        )
    }
}