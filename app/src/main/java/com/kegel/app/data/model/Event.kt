package com.kegel.app.data.model

import java.util.UUID

enum class EventType {
    KEGEL, MEDITATION
}

enum class EventStatus {
    PENDING, IN_PROGRESS, COMPLETED, MISSED
}

data class ScheduledEvent(
    val id: String = UUID.randomUUID().toString(),
    val type: EventType,
    val timeLabel: String,         // e.g., "09:45"
    val timestampMillis: Long,     // Target timestamp for event notification
    val durationMinutes: Int,      // Duration (2/3 min for Kegel, 5/10 min for Meditation)
    var status: EventStatus = EventStatus.PENDING
)
