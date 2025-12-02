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
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HistoryLocalDS"
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
    private var assetCache: List<HistoricalDraw>? = null

    override suspend fun getLocalHistory(): List<HistoricalDraw> = withContext(ioDispatcher) {
        val prefsHistory = loadFromPreferences()
        val staticHistory = getOrLoadAssetCache()

        if (prefsHistory.isEmpty()) {
            staticHistory
        } else {
            // Combina, remove duplicatas de concurso e ordena.
            // Sequência usada para evitar múltiplas listas intermediárias.
            (prefsHistory.asSequence() + staticHistory.asSequence())
                .distinctBy { it.contestNumber }
                .sortedByDescending { it.contestNumber }
                .toList()
        }
    }

    override suspend fun saveNewContests(newDraws: List<HistoricalDraw>) {
        if (newDraws.isEmpty()) return

        withContext(ioDispatcher) {
            val formattedEntries = newDraws.map { HistoryParser.formatLine(it) }.toSet()
            userPreferencesRepository.addDynamicHistoryEntries(formattedEntries)
            Log.d(TAG, "Persisted ${newDraws.size} new contests to local prefs.")
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
            context.assets.open(ASSET_FILENAME).use { inputStream ->
                inputStream.bufferedReader().useLines { lines ->
                    lines
                        .filter { it.isNotBlank() }
                        .mapNotNull { HistoryParser.parseLine(it) }
                        .toList()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading assets file: $ASSET_FILENAME", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing assets", e)
            emptyList()
        }
    }
}