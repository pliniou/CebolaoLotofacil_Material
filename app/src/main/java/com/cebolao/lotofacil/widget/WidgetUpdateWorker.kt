package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.domain.repository.GameRepository
import com.cebolao.lotofacil.domain.repository.HistoryRepository
import com.cebolao.lotofacil.util.DEFAULT_NUMBER_FORMAT
import com.cebolao.lotofacil.util.Formatters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

private const val TAG = "WidgetUpdateWorker"
private const val NUMBERS_PER_ROW = 5
private const val MAX_RETRIES = 3

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            updateLastDrawWidgets()
            updateNextContestWidgets()
            updatePinnedGameWidgets()
            Result.success()
        }.getOrElse { e ->
            Log.e(TAG, "Widget update failed", e)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private suspend fun updateLastDrawWidgets() {
        val lastDraw = historyRepository.getLastDraw()
        val ids = getWidgetIds(LastDrawWidgetProvider::class.java)
        if (ids.isEmpty()) return

        for (id in ids) {
            val views = createBaseRemoteViews(R.layout.widget_last_draw, LastDrawWidgetProvider::class.java, id)
            
            if (lastDraw != null) {
                views.setTextViewText(R.id.widget_title, "Último: ${lastDraw.contestNumber}")
                populateNumberGrid(views, R.id.widget_numbers_container, lastDraw.numbers)
                showContent(views, R.id.widget_numbers_container)
            } else {
                showError(views, R.id.widget_numbers_container, context.getString(R.string.widget_error_load))
            }
            updateAppWidget(id, views)
        }
    }

    private suspend fun updateNextContestWidgets() {
        val apiResult = historyRepository.getLatestApiResult()
        val ids = getWidgetIds(NextContestWidgetProvider::class.java)
        if (ids.isEmpty()) return

        for (id in ids) {
            val views = createBaseRemoteViews(R.layout.widget_next_contest, NextContestWidgetProvider::class.java, id)

            if (apiResult != null) {
                val nextContestNumber = apiResult.numero + 1
                val title = "${context.getString(R.string.widget_next_contest_title_generic)} $nextContestNumber"
                
                views.setTextViewText(R.id.widget_title, title)
                views.setTextViewText(R.id.widget_date, apiResult.dataProximoConcurso ?: "--/--")
                views.setTextViewText(R.id.widget_prize, Formatters.formatCurrency(apiResult.valorEstimadoProximoConcurso))
                showContent(views, R.id.widget_content)
            } else {
                showError(views, R.id.widget_content, context.getString(R.string.widget_error_load))
            }
            updateAppWidget(id, views)
        }
    }

    private suspend fun updatePinnedGameWidgets() {
        val pinnedGame = gameRepository.pinnedGames.firstOrNull()?.firstOrNull()
        val ids = getWidgetIds(PinnedGameWidgetProvider::class.java)
        if (ids.isEmpty()) return

        for (id in ids) {
            val views = createBaseRemoteViews(R.layout.widget_pinned_game, PinnedGameWidgetProvider::class.java, id)
            views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))

            if (pinnedGame != null) {
                populateNumberGrid(views, R.id.widget_numbers_container, pinnedGame.numbers)
                showContent(views, R.id.widget_numbers_container)
            } else {
                showError(views, R.id.widget_numbers_container, context.getString(R.string.widget_no_pinned_games))
            }
            updateAppWidget(id, views)
        }
    }

    // --- Helpers ---

    private fun getWidgetIds(providerClass: Class<out AppWidgetProvider>): IntArray {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, providerClass))
    }

    private fun createBaseRemoteViews(layoutId: Int, providerClass: Class<out AppWidgetProvider>, widgetId: Int): RemoteViews {
        return RemoteViews(context.packageName, layoutId).apply {
            setOnClickPendingIntent(
                R.id.widget_refresh_button,
                WidgetUtils.getRefreshPendingIntent(context, providerClass, widgetId)
            )
        }
    }

    private fun showContent(views: RemoteViews, contentId: Int) {
        views.setViewVisibility(R.id.widget_loading_text, View.GONE)
        views.setViewVisibility(contentId, View.VISIBLE)
    }

    private fun showError(views: RemoteViews, contentId: Int, message: String) {
        views.setTextViewText(R.id.widget_loading_text, message)
        views.setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
        views.setViewVisibility(contentId, View.GONE)
    }

    private fun populateNumberGrid(views: RemoteViews, containerId: Int, numbers: Set<Int>) {
        views.removeAllViews(containerId)
        
        numbers.sorted().chunked(NUMBERS_PER_ROW).forEach { rowNumbers ->
            val rowView = RemoteViews(context.packageName, R.layout.widget_numbers_row)
            rowNumbers.forEach { number ->
                val numberView = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                    setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(number))
                }
                rowView.addView(R.id.widget_numbers_row_container, numberView)
            }
            views.addView(containerId, rowView)
        }
    }
    
    private fun updateAppWidget(id: Int, views: RemoteViews) {
        AppWidgetManager.getInstance(context).updateAppWidget(id, views)
    }
}