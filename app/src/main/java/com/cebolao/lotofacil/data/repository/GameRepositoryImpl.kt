package com.cebolao.lotofacil.data.repository

import android.util.Log
import com.cebolao.lotofacil.data.LotofacilGame
import com.cebolao.lotofacil.di.ApplicationScope
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import com.cebolao.lotofacil.util.STATE_IN_TIMEOUT_MS
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GameRepository"

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val json: Json
) : GameRepository {

    private val mutex = Mutex()
    private val _allGames = MutableStateFlow<List<LotofacilGame>>(emptyList())

    // Comparador: Pinned primeiro, depois mais recentes
    private val gameComparator = compareByDescending<LotofacilGame> { it.isPinned }
        .thenByDescending { it.creationTimestamp }

    override val pinnedGames: StateFlow<ImmutableList<LotofacilGame>> = _allGames
        .map { list -> list.filter { it.isPinned }.toImmutableList() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    override val unpinnedGames: StateFlow<ImmutableList<LotofacilGame>> = _allGames
        .map { list -> list.filter { !it.isPinned }.toImmutableList() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(STATE_IN_TIMEOUT_MS), persistentListOf())

    init {
        scope.launch { loadPinnedGames() }
    }

    private suspend fun loadPinnedGames() {
        val savedJson = userPreferencesRepository.pinnedGames.first()
        val games = savedJson.mapNotNull { str ->
            runCatching { json.decodeFromString<LotofacilGame>(str) }
                .onFailure { Log.w(TAG, "Error decoding game: $str") }
                .getOrNull()
        }
        _allGames.value = games
    }

    override suspend fun addGeneratedGames(newGames: List<LotofacilGame>) = updateGames { current ->
        (current + newGames).distinctBy { it.numbers }.sortedWith(gameComparator)
    }

    override suspend fun clearUnpinnedGames() = updateGames { current ->
        current.filter { it.isPinned }
    }

    override suspend fun togglePinState(gameToToggle: LotofacilGame) = updateGames { current ->
        val updated = gameToToggle.copy(isPinned = !gameToToggle.isPinned)
        current.map { if (it.numbers == updated.numbers) updated else it }.sortedWith(gameComparator)
    }

    override suspend fun deleteGame(gameToDelete: LotofacilGame) = updateGames { current ->
        current.filterNot { it.numbers == gameToDelete.numbers }
    }

    override suspend fun exportGames(): String = json.encodeToString(_allGames.value)

    private suspend inline fun updateGames(transform: (List<LotofacilGame>) -> List<LotofacilGame>) {
        mutex.withLock {
            val oldList = _allGames.value
            val newList = transform(oldList)
            
            if (oldList != newList) {
                _allGames.value = newList
                
                // Otimização: Persistir apenas se a lista de fixados foi alterada
                val oldPinned = oldList.filter { it.isPinned }
                val newPinned = newList.filter { it.isPinned }
                if (oldPinned != newPinned) {
                    persistPinned(newPinned)
                }
            }
        }
    }

    private suspend fun persistPinned(pinned: List<LotofacilGame>) {
        val jsonSet = pinned.map { json.encodeToString(it) }.toSet()
        userPreferencesRepository.savePinnedGames(jsonSet)
    }
}