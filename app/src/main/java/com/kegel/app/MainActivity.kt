package com.kegel.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.kegel.app.data.model.EventType
import com.kegel.app.ui.screens.MainScreen
import com.kegel.app.ui.theme.KegelTheme
import com.kegel.app.ui.viewmodel.MainViewModel
import com.kegel.app.worker.ReminderWorker

data class SessionRequest(
    val eventId: String,
    val type: EventType,
    val durationMinutes: Int
)

class MainActivity : ComponentActivity() {

    private var sessionRequest by mutableStateOf<SessionRequest?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled by the OS; notification status is reactive
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionRequest = intent.toSessionRequest()
        setContent {
            KegelTheme {
                val viewModel: MainViewModel = viewModel()
                MainScreen(
                    viewModel = viewModel,
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    sessionRequest = sessionRequest,
                    onSessionRequestConsumed = { sessionRequest = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sessionRequest = intent.toSessionRequest()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun Intent?.toSessionRequest(): SessionRequest? {
        val source = this ?: return null
        if (!source.getBooleanExtra(ReminderWorker.KEY_OPEN_SESSION, false)) return null
        val eventId = source.getStringExtra(ReminderWorker.KEY_EVENT_ID).orEmpty()
        if (eventId.isBlank()) return null
        val typeName = source.getStringExtra(ReminderWorker.KEY_EVENT_TYPE) ?: EventType.KEGEL.name
        val type = runCatching { EventType.valueOf(typeName) }.getOrDefault(EventType.KEGEL)
        val duration = source.getIntExtra(ReminderWorker.KEY_EVENT_DURATION, 2)
        return SessionRequest(eventId, type, duration)
    }
}
