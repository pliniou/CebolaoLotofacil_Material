package com.cebolao.lotofacil.domain.repository

import com.cebolao.lotofacil.data.LotofacilGame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface GameRepository {
    val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>>
    val pinnedGames: StateFlow<ImmutableList<LotofacilGame>>

    suspend fun addGeneratedGames(newGames: List<LotofacilGame>)
    suspend fun clearUnpinnedGames()
    suspend fun togglePinState(gameToToggle: LotofacilGame)
    suspend fun deleteGame(gameToDelete: LotofacilGame)

    // Export utilities for sharing
    suspend fun exportGames(): String
}