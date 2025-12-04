package com.cebolao.lotofacil.domain.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import com.cebolao.lotofacil.data.GameStatisticsProvider
import com.cebolao.lotofacil.data.LotofacilConstants
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Immutable
@Serializable
data class LotofacilGame(
    override val numbers: Set<Int>,
    val isPinned: Boolean = false,
    val creationTimestamp: Long = System.currentTimeMillis()
) : GameStatisticsProvider {
    init {
        require(numbers.size == LotofacilConstants.GAME_SIZE) { 
            "Um jogo deve ter ${LotofacilConstants.GAME_SIZE} números." 
        }
        require(numbers.all { it in LotofacilConstants.NUMBER_RANGE }) { 
            "Números inválidos encontrados." 
        }
    }

    fun repeatedFrom(lastDraw: Set<Int>?): Int {
        return lastDraw?.let { numbers.intersect(it).size } ?: 0
    }
}
