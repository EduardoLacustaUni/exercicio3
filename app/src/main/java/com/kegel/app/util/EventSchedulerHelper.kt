package com.kegel.app.util

import com.kegel.app.data.model.EventStatus
import com.kegel.app.data.model.EventType
import com.kegel.app.data.model.ScheduledEvent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object EventSchedulerHelper {

    fun generateDailySchedule(
        startHour: Int,
        endHour: Int,
        kegelCount: Int,
        kegelDuration: Int,
        meditationCount: Int,
        meditationDuration: Int
    ): List<ScheduledEvent> {
        val eventsList = mutableListOf<ScheduledEvent>()
        val totalEvents = kegelCount + meditationCount
        if (totalEvents == 0) return emptyList()

        // Create pool of event properties and shuffle them
        val eventPool = mutableListOf<Pair<EventType, Int>>()
        repeat(kegelCount) { eventPool.add(Pair(EventType.KEGEL, kegelDuration)) }
        repeat(meditationCount) { eventPool.add(Pair(EventType.MEDITATION, meditationDuration)) }
        eventPool.shuffle()

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val startTime = Calendar.getInstance().apply {
            set(year, month, day, startHour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endTime = Calendar.getInstance().apply {
            set(year, month, day, endHour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val activeDuration = endTime - startTime
        if (activeDuration <= 0) return emptyList()

        val slotDuration = activeDuration / totalEvents
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        for (i in 0 until totalEvents) {
            val slotStart = startTime + i * slotDuration

            // Add 20% margin to prevent consecutive slot events from clustering at bounds
            val buffer = (slotDuration * 0.20).toLong()
            val randomOffset = if (slotDuration > 2 * buffer) {
                buffer + (Random.nextDouble() * (slotDuration - 2 * buffer)).toLong()
            } else {
                slotDuration / 2
            }

            val targetTime = slotStart + randomOffset
            val (type, duration) = eventPool[i]
            val timeLabel = timeFormatter.format(Date(targetTime))

            eventsList.add(
                ScheduledEvent(
                    type = type,
                    timeLabel = timeLabel,
                    timestampMillis = targetTime,
                    durationMinutes = duration,
                    status = EventStatus.PENDING
                )
            )
        }

        // Sort chronologically
        eventsList.sortBy { it.timestampMillis }
        return eventsList
    }
}
