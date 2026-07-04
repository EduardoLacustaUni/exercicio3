package com.kegel.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kegel.app.data.model.EventStatus
import com.kegel.app.data.model.EventType
import com.kegel.app.data.model.ScheduledEvent
import com.kegel.app.ui.theme.*
import com.kegel.app.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    // States from ViewModel
    val startHour by viewModel.startHour.collectAsState()
    val endHour by viewModel.endHour.collectAsState()
    val kegelCount by viewModel.kegelCount.collectAsState()
    val kegelDuration by viewModel.kegelDuration.collectAsState()
    val meditationCount by viewModel.meditationCount.collectAsState()
    val meditationDuration by viewModel.meditationDuration.collectAsState()
    val scheduledEvents by viewModel.scheduledEvents.collectAsState()
    val historyEvents by viewModel.historyEvents.collectAsState()
    val todayProgress by viewModel.todayProgress.collectAsState()

    // Temporary active range state for slider UI
    var tempRange by remember(startHour, endHour) {
        mutableStateOf(startHour.toFloat()..endHour.toFloat())
    }

    // Calculations for history stats
    val totalKegelsCompleted = historyEvents.count { it.type == EventType.KEGEL && it.status == EventStatus.COMPLETED }
    val totalMeditationsCompleted = historyEvents.count { it.type == EventType.MEDITATION && it.status == EventStatus.COMPLETED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BEM-ESTAR INTEGRAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Sky400,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Meu Cronograma",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate100,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Red400,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 1: Dashboard (Today's Progress)
        if (scheduledEvents.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Progresso de Hoje",
                        style = MaterialTheme.typography.titleLarge,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${(todayProgress * 100).roundToInt()}% concluído",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sky400,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { todayProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = Teal400,
                        trackColor = Slate700
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Eventos Agendados",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    scheduledEvents.forEach { event ->
                        EventRow(
                            event = event,
                            onComplete = { viewModel.updateEventStatus(event.id, EventStatus.COMPLETED) },
                            onSkip = { viewModel.updateEventStatus(event.id, EventStatus.MISSED) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.cancelAllSchedules() },
                        colors = ButtonDefaults.buttonColors(containerColor = Red400.copy(alpha = 0.2f), contentColor = Red400),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Parar Lembretes de Hoje", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Empty State Dashboard
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhum Lembrete Ativo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure e gere seu cronograma diário abaixo para ativar as notificações e iniciar suas sessões de bem-estar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Configuration Panel
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configurações da Rotina",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate100,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Os eventos serão distribuídos aleatoriamente em blocos durante seu tempo ativo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Active Period Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Período Ativo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tempRange.start.roundToInt()}:00 - ${tempRange.end.roundToInt()}:00",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sky400,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                RangeSlider(
                    value = tempRange,
                    onValueChange = { tempRange = it },
                    valueRange = 0f..24f,
                    steps = 23,
                    onValueChangeFinished = {
                        viewModel.setStartHour(tempRange.start.roundToInt())
                        viewModel.setEndHour(tempRange.end.roundToInt())
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Sky400,
                        inactiveTrackColor = Slate700,
                        thumbColor = Sky400
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(20.dp))

                // Kegel Config Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercício de Kegel",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$kegelCount por dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sky400,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = kegelCount.toFloat(),
                    onValueChange = { viewModel.setKegelCount(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Sky400,
                        inactiveTrackColor = Slate700,
                        thumbColor = Sky400
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duração de cada sessão",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3).forEach { duration ->
                            FilterChip(
                                selected = kegelDuration == duration,
                                onClick = { viewModel.setKegelDuration(duration) },
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

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Slate700)
                Spacer(modifier = Modifier.height(20.dp))

                // Meditation Config Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sessões de Meditação",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$meditationCount por dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sky400,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = meditationCount.toFloat(),
                    onValueChange = { viewModel.setMeditationCount(it.toInt()) },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Sky400,
                        inactiveTrackColor = Slate700,
                        thumbColor = Sky400
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duração de cada sessão",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10).forEach { duration ->
                            FilterChip(
                                selected = meditationDuration == duration,
                                onClick = { viewModel.setMeditationDuration(duration) },
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

                Spacer(modifier = Modifier.height(28.dp))

                // Build Schedule Button
                Button(
                    onClick = {
                        onRequestNotificationPermission()
                        viewModel.generateAndSchedulePlan()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Sky400, contentColor = Slate900)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gerar & Ativar Lembretes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Statistics History
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Histórico de Conclusões",
                        style = MaterialTheme.typography.titleLarge,
                        color = Slate100,
                        fontWeight = FontWeight.Bold
                    )
                    if (historyEvents.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Zerar histórico",
                                tint = Red400
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Kegel Stat Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Slate700)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Teal400,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$totalKegelsCompleted",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Slate100,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Kegel Feitos",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate400,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Meditation Stat Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Slate700)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Sky400,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$totalMeditationsCompleted",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Slate100,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Meditações",
                                style = MaterialTheme.typography.labelMedium,
                                color = Slate400,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventRow(
    event: ScheduledEvent,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val isCompleted = event.status == EventStatus.COMPLETED
    val isMissed = event.status == EventStatus.MISSED

    val borderCol by animateColorAsState(
        targetValue = when {
            isCompleted -> Teal400
            isMissed -> Red400
            else -> Slate700
        }, label = "rowBorderColor"
    )

    val icon = if (event.type == EventType.KEGEL) Icons.Default.Accessibility else Icons.Default.SelfImprovement
    val iconColor = if (event.type == EventType.KEGEL) Sky400 else Teal400
    val label = if (event.type == EventType.KEGEL) "Kegel" else "Meditação"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate700.copy(alpha = 0.5f))
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate800, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$label (${event.durationMinutes} min)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate100,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Horário: ${event.timeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
        }

        // Status or actions
        when (event.status) {
            EventStatus.PENDING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Red400.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pular",
                            tint = Red400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onComplete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Teal400.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Concluir",
                            tint = Teal400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            EventStatus.COMPLETED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Teal400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Concluído",
                        style = MaterialTheme.typography.bodySmall,
                        color = Teal400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            EventStatus.MISSED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Red400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Não Realizado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Red400,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
