package com.example.schedulewidget

import android.content.Context
import android.appwidget.AppWidgetManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.content.Intent

class UpdateWidgetWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(Intent(applicationContext, MyWidget::class.java).component)

        MyWidget().onUpdate(applicationContext, appWidgetManager, appWidgetIds)

        return Result.success()
    }
}
