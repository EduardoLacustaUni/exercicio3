package com.kegel.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kegel.app.data.local.PreferencesManager
import com.kegel.app.data.model.EventStatus
import com.kegel.app.data.model.EventType
import com.kegel.app.data.model.ScheduledEvent
import com.kegel.app.util.EventSchedulerHelper
import com.kegel.app.worker.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)
    private val workManager = WorkManager.getInstance(application)

    // Configuration states
    private val _startHour = MutableStateFlow(prefs.getStartHour())
    val startHour: StateFlow<Int> = _startHour.asStateFlow()

    private val _endHour = MutableStateFlow(prefs.getEndHour())
    val endHour: StateFlow<Int> = _endHour.asStateFlow()

    private val _kegelCount = MutableStateFlow(prefs.getKegelCount())
    val kegelCount: StateFlow<Int> = _kegelCount.asStateFlow()

    private val _kegelDuration = MutableStateFlow(prefs.getKegelDuration())
    val kegelDuration: StateFlow<Int> = _kegelDuration.asStateFlow()

    private val _meditationCount = MutableStateFlow(prefs.getMeditationCount())
    val meditationCount: StateFlow<Int> = _meditationCount.asStateFlow()

    private val _meditationDuration = MutableStateFlow(prefs.getMeditationDuration())
    val meditationDuration: StateFlow<Int> = _meditationDuration.asStateFlow()

    // Current daily plan & history list
    private val _scheduledEvents = MutableStateFlow<List<ScheduledEvent>>(prefs.getScheduledEvents())
    val scheduledEvents: StateFlow<List<ScheduledEvent>> = _scheduledEvents.asStateFlow()

    private val _historyEvents = MutableStateFlow<List<ScheduledEvent>>(prefs.getHistoryEvents())
    val historyEvents: StateFlow<List<ScheduledEvent>> = _historyEvents.asStateFlow()

    // Statistics computed reactively
    private val _todayProgress = MutableStateFlow(0f)
    val todayProgress: StateFlow<Float> = _todayProgress.asStateFlow()

    init {
        computeTodayProgress()
    }

    // Setters that persist data and update states
    fun setStartHour(hour: Int) {
        prefs.setStartHour(hour)
        _startHour.value = hour
    }

    fun setEndHour(hour: Int) {
        prefs.setEndHour(hour)
        _endHour.value = hour
    }

    fun setKegelCount(count: Int) {
        prefs.setKegelCount(count)
        _kegelCount.value = count
    }

    fun setKegelDuration(duration: Int) {
        prefs.setKegelDuration(duration)
        _kegelDuration.value = duration
    }

    fun setMeditationCount(count: Int) {
        prefs.setMeditationCount(count)
        _meditationCount.value = count
    }

    fun setMeditationDuration(duration: Int) {
        prefs.setMeditationDuration(duration)
        _meditationDuration.value = duration
    }

    // Core business logic: Generate schedule & program WorkManager
    fun generateAndSchedulePlan() {
        generateAndSchedulePlan(null)
    }

    fun generateAndSchedulePlan(type: EventType?) {
        val newSchedule = EventSchedulerHelper.generateDailySchedule(
            startHour = _startHour.value,
            endHour = _endHour.value,
            kegelCount = if (type == null || type == EventType.KEGEL) _kegelCount.value else 0,
            kegelDuration = _kegelDuration.value,
            meditationCount = if (type == null || type == EventType.MEDITATION) _meditationCount.value else 0,
            meditationDuration = _meditationDuration.value
        )

        val currentEvents = if (type == null) {
            emptyList()
        } else {
            _scheduledEvents.value.filter { it.type != type }
        }
        val mergedSchedule = (currentEvents + newSchedule).sortedBy { it.timestampMillis }

        prefs.saveScheduledEvents(mergedSchedule)
        _scheduledEvents.value = mergedSchedule

        scheduleEventsInWorkManager(newSchedule, type)

        computeTodayProgress()
    }

    private fun scheduleEventsInWorkManager(events: List<ScheduledEvent>, type: EventType?) {
        if (type == null) {
            workManager.cancelAllWorkByTag(TAG_DAILY_PLAN)
        } else {
            workManager.cancelAllWorkByTag(tagForType(type))
        }

        val now = System.currentTimeMillis()
        events.forEach { event ->
            if (event.timestampMillis > now) {
                val initialDelay = event.timestampMillis - now
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            ReminderWorker.KEY_EVENT_TYPE to event.type.name,
                            ReminderWorker.KEY_EVENT_DURATION to event.durationMinutes,
                            ReminderWorker.KEY_EVENT_ID to event.id
                        )
                    )
                    .addTag(TAG_DAILY_PLAN)
                    .addTag(tagForType(event.type))
                    .build()

                workManager.enqueue(workRequest)
            }
        }
    }

    fun cancelAllSchedules() {
        workManager.cancelAllWorkByTag(TAG_DAILY_PLAN)
        prefs.saveScheduledEvents(emptyList())
        _scheduledEvents.value = emptyList()
        computeTodayProgress()
    }

    fun cancelSchedules(type: EventType) {
        workManager.cancelAllWorkByTag(tagForType(type))
        val remainingEvents = _scheduledEvents.value.filter { it.type != type }
        prefs.saveScheduledEvents(remainingEvents)
        _scheduledEvents.value = remainingEvents
        computeTodayProgress()
    }

    fun updateEventStatus(eventId: String, status: EventStatus) {
        prefs.updateEventStatus(eventId, status)
        refreshEvents()

        computeTodayProgress()
    }

    fun startSession(eventId: String) {
        updateEventStatus(eventId, EventStatus.IN_PROGRESS)
    }

    fun refreshEvents() {
        _scheduledEvents.value = prefs.getScheduledEvents()
        _historyEvents.value = prefs.getHistoryEvents()
        computeTodayProgress()
    }

    private fun computeTodayProgress() {
        val today = _scheduledEvents.value
        if (today.isEmpty()) {
            _todayProgress.value = 0f
            return
        }
        val completed = today.count { it.status == EventStatus.COMPLETED }
        _todayProgress.value = completed.toFloat() / today.size.toFloat()
    }

    fun clearHistory() {
        prefs.saveHistoryEvents(emptyList())
        _historyEvents.value = emptyList()
    }

    companion object {
        const val TAG_DAILY_PLAN = "tag_daily_kegel_posture_plan"
        fun tagForType(type: EventType): String = "tag_daily_${type.name.lowercase()}_plan"
    }
}
