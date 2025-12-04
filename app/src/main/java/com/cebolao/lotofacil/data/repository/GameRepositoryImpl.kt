package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.domain.model.LotofacilGame // Corrigido
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GameRepository"

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : GameRepository {

    private val _sessionGames = MutableStateFlow<List<LotofacilGame>>(emptyList())

    override val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>> = _sessionGames
        .map { it.toImmutableList() }
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = userPreferencesRepository.pinnedGames
        .map { stringSet ->
            stringSet.mapNotNull { str ->
                runCatching { json.decodeFromString<LotofacilGame>(str) }
                    .onFailure { Log.w(TAG, "Error decoding pinned game", it) }
                    .getOrNull()
            }.sortedByDescending { it.creationTimestamp }.toImmutableList()
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) {
        _sessionGames.update { current ->
            (newGames + current).distinctBy { it.numbers }
        }
    }

    override suspend fun clearUnpinnedGames() {
        _sessionGames.value = emptyList()
    }

    override suspend fun togglePinState(gameToToggle: LotofacilGame) {
        if (gameToToggle.isPinned) {
            removeFromPinned(gameToToggle)
        } else {
            addToPinned(gameToToggle)
            _sessionGames.update { list -> list.filterNot { it.numbers == gameToToggle.numbers } }
        }
    }

    override suspend fun deleteGame(gameToDelete: LotofacilGame) {
        if (gameToDelete.isPinned) {
            removeFromPinned(gameToDelete)
        } else {
            _sessionGames.update { list -> list.filterNot { it.numbers == gameToDelete.numbers } }
        }
    }

    override suspend fun exportGames(): String = withContext(ioDispatcher) {
        val all = pinnedGames.value + unpinnedGames.value
        json.encodeToString(all)
    }

    private suspend fun addToPinned(game: LotofacilGame) {
        val pinnedGame = game.copy(isPinned = true)
        val currentJsonSet = userPreferencesRepository.pinnedGames.first()
        val newJson = json.encodeToString(pinnedGame)
        userPreferencesRepository.savePinnedGames(currentJsonSet + newJson)
    }

    private suspend fun removeFromPinned(game: LotofacilGame) {
        val currentSet = userPreferencesRepository.pinnedGames.first()
        val newSet = currentSet.filter { str ->
            val decoded = runCatching { json.decodeFromString<LotofacilGame>(str) }.getOrNull()
            decoded?.numbers != game.numbers
        }.toSet()

        userPreferencesRepository.savePinnedGames(newSet)
    }
}