package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun StatisticsExplanationCard(
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
        ) {
            TitleWithIcon(
                text = stringResource(R.string.home_understanding_stats),
                iconVector = Icons.AutoMirrored.Outlined.HelpOutline
            )

            AppDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)
            ) {
                InfoPoint(
                    title = stringResource(R.string.edu_hot_cold_title),
                    description = stringResource(R.string.edu_hot_cold_desc)
                )
                InfoPoint(
                    title = stringResource(R.string.edu_distribution_title),
                    description = stringResource(R.string.edu_distribution_desc)
                )
            }
        }
    }
}
