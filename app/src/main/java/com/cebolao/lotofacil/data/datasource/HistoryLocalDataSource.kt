package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryLocalDataSource"
private const val ASSET_FILENAME = "lotofacil_resultados.txt"

interface HistoryLocalDataSource {
    suspend fun getLocalHistory(): List<HistoricalDraw>
    suspend fun saveNewContests(newDraws: List<HistoricalDraw>)
}

@Singleton
class HistoryLocalDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HistoryLocalDataSource {

    private val cacheMutex = Mutex()
    // Cache em memória é vital pois o parse de texto é caro, 
    // mas só carregamos se necessário.
    private var assetCache: List<HistoricalDraw>? = null

    override suspend fun getLocalHistory(): List<HistoricalDraw> = withContext(ioDispatcher) {
        val prefsHistory = loadFromPreferences()
        val staticHistory = getOrLoadAssetCache()

        // Combina e remove duplicatas (priorizando prefs que podem ter updates)
        // Otimização: Se prefs estiver vazio, retorna direto o assetCache
        if (prefsHistory.isEmpty()) {
            staticHistory
        } else {
            (prefsHistory + staticHistory)
                .distinctBy { it.contestNumber }
                .sortedByDescending { it.contestNumber }
        }
    }

    override suspend fun saveNewContests(newDraws: List<HistoricalDraw>) {
        if (newDraws.isEmpty()) return

        withContext(ioDispatcher) {
            val formattedEntries = newDraws.map { HistoryParser.formatLine(it) }.toSet()
            userPreferencesRepository.addDynamicHistoryEntries(formattedEntries)
            Log.d(TAG, "Persisted ${newDraws.size} new contests.")
        }
    }

    private suspend fun loadFromPreferences(): List<HistoricalDraw> {
        val savedHistoryStrings = userPreferencesRepository.getHistory()
        return savedHistoryStrings.mapNotNull { HistoryParser.parseLine(it) }
    }

    private suspend fun getOrLoadAssetCache(): List<HistoricalDraw> {
        return cacheMutex.withLock {
            assetCache ?: loadFromAssets().also { assetCache = it }
        }
    }

    private fun loadFromAssets(): List<HistoricalDraw> {
        return try {
            context.assets.open(ASSET_FILENAME).bufferedReader().use { reader ->
                // useLines é preguiçoso (Sequence), economiza memória durante a leitura
                reader.lineSequence()
                    .filter { it.isNotBlank() }
                    .mapNotNull { HistoryParser.parseLine(it) }
                    .toList() // Materializa a lista apenas no final
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading assets file", e)
            emptyList()
        }
    }
}