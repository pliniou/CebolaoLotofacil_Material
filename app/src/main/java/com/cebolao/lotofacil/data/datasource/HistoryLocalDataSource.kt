package com.cebolao.lotofacil.data.datasource

import android.content.Context
import android.util.Log
import com.cebolao.lotofacil.data.HistoricalDraw
import com.cebolao.lotofacil.data.HistoryParser
import com.cebolao.lotofacil.di.IoDispatcher
import com.cebolao.lotofacil.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
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

    // Cache simples em memória para evitar IO repetitivo nos Assets
    private val assetCache = mutableListOf<HistoricalDraw>()

    override suspend fun getLocalHistory(): List<HistoricalDraw> = withContext(ioDispatcher) {
        try {
            if (assetCache.isEmpty()) {
                assetCache.addAll(loadFromAssets())
            }

            val savedHistoryStrings = userPreferencesRepository.getHistory()
            val savedDraws = savedHistoryStrings.mapNotNull { HistoryParser.parseLine(it) }

            (savedDraws + assetCache)
                .distinctBy { it.contestNumber }
                .sortedByDescending { it.contestNumber }
                .also {
                    Log.d(TAG, "Loaded ${it.size} contests (Assets: ${assetCache.size}, Prefs: ${savedDraws.size})")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load local history", e)
            emptyList()
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

    private fun loadFromAssets(): List<HistoricalDraw> {
        return try {
            context.assets.open(ASSET_FILENAME).bufferedReader().use { reader ->
                reader.lineSequence()
                    .filter { it.isNotBlank() }
                    .mapNotNull { HistoryParser.parseLine(it) }
                    .toList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading assets file", e)
            emptyList()
        }
    }
}