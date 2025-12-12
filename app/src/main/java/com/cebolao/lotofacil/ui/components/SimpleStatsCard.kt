package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    SectionCard(
        modifier = modifier,
        title = stringResource(R.string.checker_simple_stats_title)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
        ) {
            stats.forEachIndexed { index, (label, value) ->
                InfoValueRow(label = label, value = value)
                if (index < stats.lastIndex) {
                    AppDivider()
                }
            }
        }
    }
}
