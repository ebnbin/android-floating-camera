package com.ebnbin.floatingcamera.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.ebnbin.floatingcamera.R
import com.ebnbin.floatingcamera.service.CameraService

class CameraAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        context ?: return
        appWidgetManager ?: return
        appWidgetIds ?: return

        val intent = Intent(context, CameraService::class.java).putExtra(KEY_FROM, "widget")
        val pendingIntent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            PendingIntent.getService(context, 0, intent, 0)
        } else {
            PendingIntent.getForegroundService(context, 0, intent, 0)
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.camera_app_widget_provider)
        remoteViews.setOnClickPendingIntent(R.id.camera, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    companion object {
        const val KEY_FROM = "from"
    }
}
