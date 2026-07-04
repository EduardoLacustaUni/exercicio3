package com.kegel.app.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kegel.app.KegelApplication
import com.kegel.app.MainActivity
import com.kegel.app.data.model.EventType
import kotlin.random.Random

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val eventTypeStr = inputData.getString(KEY_EVENT_TYPE) ?: EventType.KEGEL.name
        val duration = inputData.getInt(KEY_EVENT_DURATION, 2)
        val eventId = inputData.getString(KEY_EVENT_ID) ?: ""

        sendReminderNotification(eventTypeStr, duration, eventId)
        return Result.success()
    }

    private fun sendReminderNotification(typeStr: String, duration: Int, eventId: String) {
        val type = try { EventType.valueOf(typeStr) } catch (e: Exception) { EventType.KEGEL }

        val title: String
        val text: String

        if (type == EventType.KEGEL) {
            val messages = listOf(
                "Prepare-se para o seu exercício de Kegel de $duration minutos.",
                "Hora de contrair e relaxar! Sua sessão de $duration minutos está pronta.",
                "Momento Kegel: invista $duration minutos na sua saúde pélvica!"
            )
            title = "Exercício de Kegel"
            text = messages[Random.nextInt(messages.size)]
        } else {
            val messages = listOf(
                "Sua pausa para meditação de $duration minutos. Vamos desacelerar?",
                "Hora de se conectar com você mesmo: meditação de $duration minutos.",
                "Respire fundo. Dedique $duration minutos à sua mente agora."
            )
            title = "Momento Meditação"
            text = messages[Random.nextInt(messages.size)]
        }

        // Open MainActivity when clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_EVENT_TYPE, typeStr)
            putExtra(KEY_EVENT_DURATION, duration)
            putExtra(KEY_EVENT_ID, eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            Random.nextInt(), // unique request code to prevent overwriting
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, KegelApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(Random.nextInt(), builder.build())
        }
    }

    companion object {
        const val KEY_EVENT_TYPE = "KEY_EVENT_TYPE"
        const val KEY_EVENT_DURATION = "KEY_EVENT_DURATION"
        const val KEY_EVENT_ID = "KEY_EVENT_ID"
    }
}
