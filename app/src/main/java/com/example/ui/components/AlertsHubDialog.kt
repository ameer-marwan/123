package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AlertFrequency
import com.example.data.model.AlertSeverity
import com.example.data.model.FarmAlert
import com.example.viewmodel.FieldShiftViewModel

@Composable
fun AlertsHubDialog(
    viewModel: FieldShiftViewModel,
    onDismiss: () -> Unit
) {
    val alerts by viewModel.alerts.collectAsState()
    val settings by viewModel.alertSettings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active Alerts, 1 = Threshold Settings

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .testTag("alerts_hub_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with unread count & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Real-Time Field Alerts & Anomalies",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val activeCount = alerts.count { !it.isAcknowledged && !it.isSnoozed }
                            Text(
                                text = "$activeCount active environmental triggers requiring action",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Alerts vs Notification Settings
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active Alerts (${alerts.count { !it.isAcknowledged }})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Thresholds & Push", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // --- ACTIVE ALERTS LIST ---
                    val visibleAlerts = alerts.filter { !it.isAcknowledged }

                    if (visibleAlerts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(42.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("All Fields In Optimal Range", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("No severe weather, soil moisture, or NDVI anomalies detected.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(visibleAlerts, key = { it.id }) { alert ->
                                AlertItemCard(
                                    alert = alert,
                                    onAcknowledge = { viewModel.acknowledgeAlert(alert.id) },
                                    onSnooze = { viewModel.snoozeAlert(alert.id) }
                                )
                            }
                        }
                    }
                } else {
                    // --- THRESHOLD & NOTIFICATION SETTINGS ---
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Dispatch Channels", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Push Notifications
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("In-App Push Alerts", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("Banner popups when NASA satellites register anomalies", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(
                                            checked = settings.pushNotificationsEnabled,
                                            onCheckedChange = { viewModel.updateAlertSettings(settings.copy(pushNotificationsEnabled = it)) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Email Alerts
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Email Dispatch", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("Deliver telemetry alert reports to inbox", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(
                                            checked = settings.emailNotificationsEnabled,
                                            onCheckedChange = { viewModel.updateAlertSettings(settings.copy(emailNotificationsEnabled = it)) }
                                        )
                                    }

                                    if (settings.emailNotificationsEnabled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = settings.emailAddress,
                                            onValueChange = { viewModel.updateAlertSettings(settings.copy(emailAddress = it)) },
                                            label = { Text("Alert Recipient Email", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Configurable Trigger Thresholds", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Frost Threshold
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Ground Frost Trigger (°C)", fontSize = 12.sp)
                                            Text("${settings.frostThresholdC}°C", fontWeight = FontWeight.Bold, color = Color(0xFF40C4FF))
                                        }
                                        Slider(
                                            value = settings.frostThresholdC.toFloat(),
                                            onValueChange = { viewModel.updateAlertSettings(settings.copy(frostThresholdC = it.toDouble())) },
                                            valueRange = -5f..6f,
                                            steps = 11
                                        )
                                    }

                                    // Heatwave Threshold
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Extreme Heatwave Trigger (°C)", fontSize = 12.sp)
                                            Text("${settings.heatwaveThresholdC.toInt()}°C", fontWeight = FontWeight.Bold, color = Color(0xFFFF7043))
                                        }
                                        Slider(
                                            value = settings.heatwaveThresholdC.toFloat(),
                                            onValueChange = { viewModel.updateAlertSettings(settings.copy(heatwaveThresholdC = it.toDouble())) },
                                            valueRange = 28f..45f,
                                            steps = 17
                                        )
                                    }

                                    // Moisture Deficit Threshold
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Critical Soil Moisture Deficit (%)", fontSize = 12.sp)
                                            Text("${settings.moistureMinPercent}% m³/m³", fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                                        }
                                        Slider(
                                            value = settings.moistureMinPercent.toFloat(),
                                            onValueChange = { viewModel.updateAlertSettings(settings.copy(moistureMinPercent = it.toInt())) },
                                            valueRange = 8f..24f,
                                            steps = 16
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(
    alert: FarmAlert,
    onAcknowledge: () -> Unit,
    onSnooze: () -> Unit
) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Color(0xFFFF5252)
        AlertSeverity.WARNING -> Color(0xFFFFB300)
        AlertSeverity.INFO -> Color(0xFF29B6F6)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = severityColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = alert.severity.label.uppercase(),
                        color = severityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = alert.farmName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Observed: ${alert.parameterObserved}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = severityColor
                    )
                    Text(
                        text = "Agronomist Action: ${alert.recommendedAction}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze (24h)", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAcknowledge,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Acknowledge", fontSize = 11.sp)
                }
            }
        }
    }
}
