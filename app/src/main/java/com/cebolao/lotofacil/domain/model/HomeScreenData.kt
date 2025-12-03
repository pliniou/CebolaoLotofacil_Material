package com.cebolao.lotofacil.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.StatisticsReport

@Stable
@Immutable
data class NextDrawInfo(
    val contestNumber: Int,
    val formattedDate: String,
    val formattedPrize: String,
    val formattedPrizeFinalFive: String
)

@Stable
@Immutable
data class WinnerData(
    val hits: Int,
    val description: String,
    val winnerCount: Int,
    val prize: Double
)

@Stable
@Immutable
data class HomeScreenData(
    val lastDraw: HistoricalDraw?,
    val initialStats: StatisticsReport,
    val nextDrawInfo: NextDrawInfo?,
    val winnerData: List<WinnerData>,
    val lastDrawCheckResult: CheckResult? = null // Adicionado para suportar a lógica
)