package com.cebolao.lotofacil.data

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.data.network.LotofacilApiResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

private const val TAG = "HistoricalDraw"

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class HistoricalDraw(
    val contestNumber: Int,
    override val numbers: Set<Int>,
    val date: String? = null
) : GameStatisticsProvider {

    companion object {
        fun fromApiResult(apiResult: LotofacilApiResult): HistoricalDraw? {
            return runCatching {
                val contest = apiResult.numero
                val numbers = apiResult.listaDezenas.mapNotNull { it.toIntOrNull() }.toSet()

                require(contest > 0) { "Número de concurso inválido: $contest" }
                require(numbers.size == LotofacilConstants.GAME_SIZE) {
                    "Contagem inválida para concurso $contest: ${numbers.size}"
                }
                require(numbers.all { it in LotofacilConstants.NUMBER_RANGE }) {
                    "Números fora do intervalo para concurso $contest"
                }

                HistoricalDraw(contest, numbers, apiResult.dataApuracao)
            }.onFailure { e ->
                Log.w(TAG, "Falha ao processar API (Conc: ${apiResult.numero}): ${e.message}")
            }.getOrNull()
        }
    }
}

@Immutable
data class CheckResult(
    val scoreCounts: ImmutableMap<Int, Int> = persistentMapOf(),
    val lastHitContest: Int? = null,
    val lastHitScore: Int? = null,
    val lastCheckedContest: Int,
    val recentHits: ImmutableList<Pair<Int, Int>> = persistentListOf()
)