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

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val historyRepository: HistoryRepository,
    private val gameRepository: GameRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            updateLastDrawWidgets()
            updateNextContestWidgets()
            updatePinnedGameWidgets()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget update failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun updateLastDrawWidgets() {
        updateWidgets(
            providerClass = LastDrawWidgetProvider::class.java,
            layoutId = R.layout.widget_last_draw,
            contentViewId = R.id.widget_numbers_container,
            fetchData = { historyRepository.getLastDraw() },
            updateViews = { lastDraw ->
                setTextViewText(R.id.widget_title, "Concurso ${lastDraw.contestNumber}")
                populateNumberGrid(this, lastDraw.numbers)
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_last_draw_title))
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
            }
        )
    }

    private suspend fun updateNextContestWidgets() {
        updateWidgets(
            providerClass = NextContestWidgetProvider::class.java,
            layoutId = R.layout.widget_next_contest,
            contentViewId = R.id.widget_content,
            fetchData = { historyRepository.getLatestApiResult() },
            updateViews = { info ->
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_next_contest_title, info.numero + 1))
                setTextViewText(R.id.widget_date, info.dataProximoConcurso ?: "--/--")
                setTextViewText(R.id.widget_prize, Formatters.formatCurrency(info.valorEstimadoProximoConcurso))
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_error_load))
            }
        )
    }

    private suspend fun updatePinnedGameWidgets() {
        updateWidgets(
            providerClass = PinnedGameWidgetProvider::class.java,
            layoutId = R.layout.widget_pinned_game,
            contentViewId = R.id.widget_numbers_container,
            // Pega o primeiro jogo da lista de flow ou null
            fetchData = { gameRepository.pinnedGames.firstOrNull()?.firstOrNull() },
            updateViews = { game ->
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                populateNumberGrid(this, game.numbers)
            },
            updateErrorViews = {
                setTextViewText(R.id.widget_title, context.getString(R.string.widget_pinned_game_title))
                setTextViewText(R.id.widget_loading_text, context.getString(R.string.widget_no_pinned_games))
            }
        )
    }

    private fun populateNumberGrid(views: RemoteViews, numbers: Set<Int>) {
        views.removeAllViews(R.id.widget_numbers_container)
        
        numbers.sorted().chunked(NUMBERS_PER_ROW).forEach { rowNumbers ->
            val rowView = RemoteViews(context.packageName, R.layout.widget_numbers_row)
            rowNumbers.forEach { number ->
                val numberView = RemoteViews(context.packageName, R.layout.widget_number_ball).apply {
                    setTextViewText(R.id.widget_ball_text, DEFAULT_NUMBER_FORMAT.format(number))
                }
                rowView.addView(R.id.widget_numbers_row_container, numberView)
            }
            views.addView(R.id.widget_numbers_container, rowView)
        }
    }

    private suspend fun <T> updateWidgets(
        providerClass: Class<out AppWidgetProvider>,
        layoutId: Int,
        contentViewId: Int,
        fetchData: suspend () -> T?,
        updateViews: RemoteViews.(data: T) -> Unit,
        updateErrorViews: RemoteViews.() -> Unit
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass))
        if (ids.isEmpty()) return

        val data = runCatching { fetchData() }.getOrNull()

        for (id in ids) {
            val views = RemoteViews(context.packageName, layoutId).apply {
                setOnClickPendingIntent(
                    R.id.widget_refresh_button,
                    WidgetUtils.getRefreshPendingIntent(context, providerClass, id)
                )

                if (data != null) {
                    updateViews(this, data)
                    setViewVisibility(R.id.widget_loading_text, View.GONE)
                    setViewVisibility(contentViewId, View.VISIBLE)
                } else {
                    updateErrorViews(this)
                    setViewVisibility(R.id.widget_loading_text, View.VISIBLE)
                    setViewVisibility(contentViewId, View.GONE)
                }
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}