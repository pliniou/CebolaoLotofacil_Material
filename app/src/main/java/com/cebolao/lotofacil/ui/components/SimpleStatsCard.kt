package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SimpleStatsCard(
    stats: ImmutableList<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)) {
            Text(
                text = stringResource(R.string.checker_simple_stats_title),
                style = MaterialTheme.typography.titleMedium
            )
            AppDivider()
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                stats.forEach { (label, value) ->
                    InfoValueRow(label = label, value = value)
                }
            }
        }
    }
}