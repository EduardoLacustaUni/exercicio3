package com.kegel.app.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kegel.app.data.local.PreferencesManager
import com.kegel.app.data.model.EventStatus

class MissedReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val eventId = inputData.getString(ReminderWorker.KEY_EVENT_ID) ?: return Result.success()
        val prefs = PreferencesManager(applicationContext)
        val event = prefs.getScheduledEvents().find { it.id == eventId } ?: return Result.success()

        if (event.status == EventStatus.PENDING) {
            prefs.updateEventStatus(eventId, EventStatus.MISSED)
            NotificationManagerCompat.from(applicationContext).cancel(eventId.hashCode())
        }

        return Result.success()
    }
}
