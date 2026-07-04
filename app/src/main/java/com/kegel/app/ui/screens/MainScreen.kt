package com.kegel.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kegel.app.SessionRequest
import com.kegel.app.data.model.EventStatus
import com.kegel.app.data.model.EventType
import com.kegel.app.ui.theme.Red400
import com.kegel.app.ui.theme.Sky400
import com.kegel.app.ui.theme.Slate100
import com.kegel.app.ui.theme.Slate400
import com.kegel.app.ui.theme.Slate700
import com.kegel.app.ui.theme.Slate800
import com.kegel.app.ui.theme.Slate900
import com.kegel.app.ui.theme.Teal400
import com.kegel.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class AppPage { HOME, SETTINGS, SESSION }

private data class ActiveSession(
    val eventId: String,
    val type: EventType,
    val durationMinutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestNotificationPermission: () -> Unit,
    sessionRequest: SessionRequest?,
    onSessionRequestConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startHour by viewModel.startHour.collectAsState()
    val endHour by viewModel.endHour.collectAsState()
    val kegelCount by viewModel.kegelCount.collectAsState()
    val kegelDuration by viewModel.kegelDuration.collectAsState()
    val meditationCount by viewModel.meditationCount.collectAsState()
    val meditationDuration by viewModel.meditationDuration.collectAsState()
    val scheduledEvents by viewModel.scheduledEvents.collectAsState()
    val historyEvents by viewModel.historyEvents.collectAsState()
    val todayProgress by viewModel.todayProgress.collectAsState()

    var page by remember { mutableStateOf(AppPage.HOME) }
    var activeSession by remember { mutableStateOf<ActiveSession?>(null) }

    LaunchedEffect(sessionRequest) {
        sessionRequest?.let {
            activeSession = ActiveSession(it.eventId, it.type, it.durationMinutes)
            viewModel.startSession(it.eventId)
            viewModel.refreshEvents()
            page = AppPage.SESSION
            onSessionRequestConsumed()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (page) {
                            AppPage.HOME -> "Kegel & Meditação"
                            AppPage.SETTINGS -> "Parâmetros"
                            AppPage.SESSION -> "Sessão"
                        },
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (page != AppPage.SESSION) {
                        IconButton(onClick = { page = if (page == AppPage.HOME) AppPage.SETTINGS else AppPage.HOME }) {
                            Icon(
                                imageVector = if (page == AppPage.HOME) Icons.Default.Settings else Icons.Default.Close,
                                contentDescription = if (page == AppPage.HOME) "Abrir parâmetros" else "Voltar",
                                tint = Slate100
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when (page) {
            AppPage.HOME -> HomeScreen(
                scheduledEvents = scheduledEvents,
                historyEvents = historyEvents,
                todayProgress = todayProgress,
                onToggleKegel = {
                    onRequestNotificationPermission()
                    if (scheduledEvents.hasActiveType(EventType.KEGEL)) {
                        viewModel.cancelSchedules(EventType.KEGEL)
                    } else {
                        viewModel.generateAndSchedulePlan(EventType.KEGEL)
                    }
                },
                onToggleMeditation = {
                    onRequestNotificationPermission()
                    if (scheduledEvents.hasActiveType(EventType.MEDITATION)) {
                        viewModel.cancelSchedules(EventType.MEDITATION)
                    } else {
                        viewModel.generateAndSchedulePlan(EventType.MEDITATION)
                    }
                },
                onOpenSettings = { page = AppPage.SETTINGS },
                modifier = Modifier.padding(padding)
            )
            AppPage.SETTINGS -> SettingsScreen(
                startHour = startHour,
                endHour = endHour,
                kegelCount = kegelCount,
                kegelDuration = kegelDuration,
                meditationCount = meditationCount,
                meditationDuration = meditationDuration,
                onStartHourChange = viewModel::setStartHour,
                onEndHourChange = viewModel::setEndHour,
                onKegelCountChange = viewModel::setKegelCount,
                onKegelDurationChange = viewModel::setKegelDuration,
                onMeditationCountChange = viewModel::setMeditationCount,
                onMeditationDurationChange = viewModel::setMeditationDuration,
                onBack = { page = AppPage.HOME },
                modifier = Modifier.padding(padding)
            )
            AppPage.SESSION -> SessionTimerScreen(
                session = activeSession,
                onCompleted = { eventId ->
                    viewModel.updateEventStatus(eventId, EventStatus.COMPLETED)
                    activeSession = null
                    page = AppPage.HOME
                },
                onMissed = { eventId ->
                    viewModel.updateEventStatus(eventId, EventStatus.MISSED)
                    activeSession = null
                    page = AppPage.HOME
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HomeScreen(
    scheduledEvents: List<com.kegel.app.data.model.ScheduledEvent>,
    historyEvents: List<com.kegel.app.data.model.ScheduledEvent>,
    todayProgress: Float,
    onToggleKegel: () -> Unit,
    onToggleMeditation: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isKegelActive = scheduledEvents.hasActiveType(EventType.KEGEL)
    val isMeditationActive = scheduledEvents.hasActiveType(EventType.MEDITATION)
    val totalKegelsCompleted = historyEvents.count { it.type == EventType.KEGEL && it.status == EventStatus.COMPLETED }
    val totalMeditationsCompleted = historyEvents.count { it.type == EventType.MEDITATION && it.status == EventStatus.COMPLETED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LogoMark()
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Bem-estar em pequenas pausas",
            style = MaterialTheme.typography.headlineSmall,
            color = Slate100,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Ative lembretes discretos e registre cada sessão quando ela acontecer.",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate400,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))
        ActionCard(
            title = "Exercício de Kegel",
            subtitle = if (isKegelActive) "Notificações ativas" else "Notificações pausadas",
            icon = Icons.Default.Accessibility,
            accent = Sky400,
            active = isKegelActive,
            onClick = onToggleKegel
        )
        Spacer(modifier = Modifier.height(14.dp))
        ActionCard(
            title = "Meditações",
            subtitle = if (isMeditationActive) "Notificações ativas" else "Notificações pausadas",
            icon = Icons.Default.SelfImprovement,
            accent = Teal400,
            active = isMeditationActive,
            onClick = onToggleMeditation
        )

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate100)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajustar parâmetros", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(22.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Progresso de hoje", color = Slate100, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { todayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(CircleShape),
                    color = Teal400,
                    trackColor = Slate700
                )
                Text(
                    text = "${(todayProgress * 100).roundToInt()}% concluído",
                    color = Sky400,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatPill("Kegel", totalKegelsCompleted.toString(), Sky400, Modifier.weight(1f))
                    StatPill("Meditações", totalMeditationsCompleted.toString(), Teal400, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(116.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Sky400, Teal400)))
            .border(6.dp, Slate800, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Slate900,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    active: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Slate100, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Slate400, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (active) Red400.copy(alpha = 0.18f) else accent,
                    contentColor = if (active) Red400 else Slate900
                )
            ) {
                Icon(
                    imageVector = if (active) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (active) "Parar" else "Ativar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Slate700)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Slate400, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SessionTimerScreen(
    session: ActiveSession?,
    onCompleted: (String) -> Unit,
    onMissed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (session == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma sessão ativa", color = Slate400)
        }
        return
    }

    var remainingSeconds by remember(session.eventId) { mutableIntStateOf(session.durationMinutes * 60) }
    var showResultDialog by remember(session.eventId) { mutableStateOf(false) }
    val totalSeconds = session.durationMinutes * 60
    val progress = if (totalSeconds == 0) 1f else remainingSeconds.toFloat() / totalSeconds.toFloat()
    val title = if (session.type == EventType.KEGEL) "Exercício de Kegel" else "Meditação"

    LaunchedEffect(session.eventId) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
        showResultDialog = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (session.type == EventType.KEGEL) Icons.Default.Accessibility else Icons.Default.SelfImprovement,
            contentDescription = null,
            tint = if (session.type == EventType.KEGEL) Sky400 else Teal400,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(title, color = Slate100, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = formatTime(remainingSeconds),
            color = Slate100,
            fontSize = 54.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = if (session.type == EventType.KEGEL) Sky400 else Teal400,
            trackColor = Slate700
        )
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Sessão finalizada") },
            text = { Text("Você realizou a atividade?") },
            confirmButton = {
                TextButton(onClick = { onCompleted(session.eventId) }) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sim")
                }
            },
            dismissButton = {
                TextButton(onClick = { onMissed(session.eventId) }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Não")
                }
            }
        )
    }
}

@Composable
private fun SettingsScreen(
    startHour: Int,
    endHour: Int,
    kegelCount: Int,
    kegelDuration: Int,
    meditationCount: Int,
    meditationDuration: Int,
    onStartHourChange: (Int) -> Unit,
    onEndHourChange: (Int) -> Unit,
    onKegelCountChange: (Int) -> Unit,
    onKegelDurationChange: (Int) -> Unit,
    onMeditationCountChange: (Int) -> Unit,
    onMeditationDurationChange: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tempRange by remember(startHour, endHour) {
        mutableStateOf(startHour.toFloat()..endHour.toFloat())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Configurações da rotina", color = Slate100, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("A agenda diária usa esses parâmetros quando você ativa cada tipo de lembrete.", color = Slate400, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(24.dp))

                ConfigHeader("Período ativo", "${tempRange.start.roundToInt()}:00 - ${tempRange.endInclusive.roundToInt()}:00")
                RangeSlider(
                    value = tempRange,
                    onValueChange = { tempRange = it },
                    valueRange = 0f..24f,
                    steps = 23,
                    onValueChangeFinished = {
                        onStartHourChange(tempRange.start.roundToInt())
                        onEndHourChange(tempRange.endInclusive.roundToInt())
                    },
                    colors = SliderDefaults.colors(activeTrackColor = Sky400, inactiveTrackColor = Slate700, thumbColor = Sky400)
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(18.dp))

                ConfigHeader("Exercício de Kegel", "$kegelCount por dia")
                Slider(
                    value = kegelCount.toFloat(),
                    onValueChange = { onKegelCountChange(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(activeTrackColor = Sky400, inactiveTrackColor = Slate700, thumbColor = Sky400)
                )
                DurationChips(
                    label = "Duração de cada sessão",
                    values = listOf(2, 3),
                    selected = kegelDuration,
                    onSelected = onKegelDurationChange
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(18.dp))

                ConfigHeader("Meditações", "$meditationCount por dia")
                Slider(
                    value = meditationCount.toFloat(),
                    onValueChange = { onMeditationCountChange(it.toInt()) },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(activeTrackColor = Teal400, inactiveTrackColor = Slate700, thumbColor = Teal400)
                )
                DurationChips(
                    label = "Duração de cada sessão",
                    values = listOf(5, 10),
                    selected = meditationDuration,
                    onSelected = onMeditationDurationChange
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Sky400, contentColor = Slate900)
        ) {
            Text("Concluir", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConfigHeader(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Slate100, fontWeight = FontWeight.Bold)
        Text(value, color = Sky400, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DurationChips(
    label: String,
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Slate400, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { duration ->
                FilterChip(
                    selected = selected == duration,
                    onClick = { onSelected(duration) },
                    label = { Text("$duration min") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Sky400,
                        selectedLabelColor = Slate900,
                        containerColor = Slate700,
                        labelColor = Slate100
                    ),
                    border = null
                )
            }
        }
    }
}

private fun List<com.kegel.app.data.model.ScheduledEvent>.hasActiveType(type: EventType): Boolean {
    return any { it.type == type && (it.status == EventStatus.PENDING || it.status == EventStatus.IN_PROGRESS) }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
