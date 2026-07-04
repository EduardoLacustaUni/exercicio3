package com.kegel.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kegel.app.data.model.ScheduledEvent

class PreferencesManager(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("kegel_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getStartHour(): Int = sharedPrefs.getInt("start_hour", 8)
    fun setStartHour(hour: Int) = sharedPrefs.edit().putInt("start_hour", hour).apply()

    fun getEndHour(): Int = sharedPrefs.getInt("end_hour", 22)
    fun setEndHour(hour: Int) = sharedPrefs.edit().putInt("end_hour", hour).apply()

    fun getKegelCount(): Int = sharedPrefs.getInt("kegel_count", 5)
    fun setKegelCount(count: Int) = sharedPrefs.edit().putInt("kegel_count", count).apply()

    fun getKegelDuration(): Int = sharedPrefs.getInt("kegel_duration", 2)
    fun setKegelDuration(duration: Int) = sharedPrefs.edit().putInt("kegel_duration", duration).apply()

    fun getMeditationCount(): Int = sharedPrefs.getInt("meditation_count", 1)
    fun setMeditationCount(count: Int) = sharedPrefs.edit().putInt("meditation_count", count).apply()

    fun getMeditationDuration(): Int = sharedPrefs.getInt("meditation_duration", 5)
    fun setMeditationDuration(duration: Int) = sharedPrefs.edit().putInt("meditation_duration", duration).apply()

    fun getScheduledEvents(): List<ScheduledEvent> {
        val json = sharedPrefs.getString("scheduled_events", null) ?: return emptyList()
        val type = object : TypeToken<List<ScheduledEvent>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveScheduledEvents(events: List<ScheduledEvent>) {
        val json = gson.toJson(events)
        sharedPrefs.edit().putString("scheduled_events", json).apply()
    }

    fun getHistoryEvents(): List<ScheduledEvent> {
        val json = sharedPrefs.getString("history_events", null) ?: return emptyList()
        val type = object : TypeToken<List<ScheduledEvent>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveHistoryEvents(events: List<ScheduledEvent>) {
        val json = gson.toJson(events)
        sharedPrefs.edit().putString("history_events", json).apply()
    }
}
