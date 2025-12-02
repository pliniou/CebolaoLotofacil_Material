package com.cebolao.lotofacil.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.cebolao.lotofacil.util.ACTION_REFRESH

/**
 * Classe base abstrata para todos os widgets do app.
 * Centraliza a lógica de atualização e tratamento de intents de refresh.
 */
abstract class BaseLotofacilWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Mostra estado de loading imediato em todas as instâncias
        for (appWidgetId in appWidgetIds) {
            WidgetUtils.showLoading(context, this::class.java, appWidgetId)
        }
        // Enfileira o Worker para buscar dados em background
        WidgetUtils.enqueueOneTimeWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetUtils.showLoading(context, this::class.java, appWidgetId)
                WidgetUtils.enqueueOneTimeWidgetUpdate(context)
            }
        }
    }
}