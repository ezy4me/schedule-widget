package com.example.schedulewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class MyWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun loadSchedule(context: Context): String {
        return try {
            val inputStream: InputStream = context.assets.open("schedule.json")
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            "{}"
        }
    }

    private fun isEvenWeek(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val weekOfYear = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        return weekOfYear % 2 == 0
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val jsonString = loadSchedule(context)
        val schedule = JSONObject(jsonString)

        val currentDayEnglish = getCurrentDayEnglish()
        val daySchedule = schedule.optJSONArray(currentDayEnglish)

        val views = RemoteViews(context.packageName, R.layout.my_widget)
        views.setTextViewText(R.id.appwidget_current_day, getCurrentDayRussian(currentDayEnglish))

        val weekType = if (isEvenWeek()) "Четная" else "Нечетная"
        views.setTextViewText(R.id.appwidget_week_type, weekType)

        views.removeAllViews(R.id.schedule_container)

        val filteredSchedule = filterSchedule(daySchedule)

        if (filteredSchedule.isNotEmpty()) {
            for (classInfo in filteredSchedule) {
                views.addView(R.id.schedule_container, createRow(context, classInfo))
            }
        } else {
            views.addView(R.id.schedule_container, createRow(context, "Нет данных", "", "", "", "", ""))
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun filterSchedule(daySchedule: JSONArray?): List<ScheduleItem> {
        val filteredSchedule = mutableListOf<ScheduleItem>()
        val isEvenWeek = isEvenWeek()

        if (daySchedule != null) {
            for (i in 0 until daySchedule.length()) {
                val classInfo = daySchedule.getJSONObject(i)
                val dateInfo = classInfo.optString("Date", "").trim()

                if ((dateInfo == "чет" && isEvenWeek) || (dateInfo == "неч" && !isEvenWeek)) {
                    filteredSchedule.add(ScheduleItem.fromJson(classInfo))
                } else if (dateInfo.isEmpty() || (!dateInfo.contains("чет") && !dateInfo.contains("неч"))) {
                    filteredSchedule.add(ScheduleItem.fromJson(classInfo))
                }
            }
        }
        return filteredSchedule
    }

    private fun createRow(context: Context, scheduleItem: ScheduleItem): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.schedule_row)

        row.setTextViewText(R.id.time, scheduleItem.time)
        row.setTextViewText(R.id.discipline, scheduleItem.discipline)
        row.setTextViewText(R.id.room, scheduleItem.room)
        row.setTextViewText(R.id.teacher, scheduleItem.teacher)
        row.setTextViewText(R.id.type, scheduleItem.type)

        // Установка видимости даты
        if (scheduleItem.date != "чет" && scheduleItem.date != "неч" && scheduleItem.date.isNotEmpty()) {
            row.setTextViewText(R.id.date, scheduleItem.date)
            row.setViewVisibility(R.id.date, View.VISIBLE)
        } else {
            row.setTextViewText(R.id.date, "")
            row.setViewVisibility(R.id.date, View.GONE)
        }

        return row
    }

    private fun createRow(context: Context, time: String, discipline: String, room: String, teacher: String, type: String, date: String): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.schedule_row)

        row.setTextViewText(R.id.time, time)
        row.setTextViewText(R.id.discipline, discipline)
        row.setTextViewText(R.id.room, room)
        row.setTextViewText(R.id.teacher, teacher)
        row.setTextViewText(R.id.type, type)

        if (date != "чет" && date != "неч") {
            row.setTextViewText(R.id.date, date)
        } else {
            row.setTextViewText(R.id.date, "")
        }

        return row
    }

    private fun getCurrentDayEnglish(): String {
        val daysOfWeek = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val currentDayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
        return daysOfWeek[currentDayIndex]
    }

    private fun getCurrentDayRussian(englishDay: String): String {
        return when (englishDay) {
            "Sunday" -> "Воскресенье"
            "Monday" -> "Понедельник"
            "Tuesday" -> "Вторник"
            "Wednesday" -> "Среда"
            "Thursday" -> "Четверг"
            "Friday" -> "Пятница"
            "Saturday" -> "Суббота"
            else -> "Неизвестный день"
        }
    }

    data class ScheduleItem(
        val time: String,
        val discipline: String,
        val room: String,
        val teacher: String,
        val type: String,
        val date: String
    ) {
        companion object {
            fun fromJson(jsonObject: JSONObject): ScheduleItem {
                return ScheduleItem(
                    time = jsonObject.getString("Time"),
                    discipline = jsonObject.getString("Discipline"),
                    room = jsonObject.getString("Room"),
                    teacher = jsonObject.getString("Teacher"),
                    type = jsonObject.getString("Type"),
                    date = jsonObject.getString("Date")
                )
            }
        }
    }
}
