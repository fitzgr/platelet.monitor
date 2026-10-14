package com.example.plateletmonitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text

@Composable
fun MonitorScreen(
    state: MonitorUiState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 18.dp)
    ) {
        item {
            Text(
                text = state.statusMessage,
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = state.gsrMicrosiemens?.let { "%.2f".format(it) } ?: "—",
                style = MaterialTheme.typography.display1,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = when {
                    state.gsrMicrosiemens != null -> "GSR / cEDA · μS"
                    state.hasGsrSensor -> "GSR / cEDA · waiting for data"
                    else -> "GSR not exposed to this app"
                },
                style = MaterialTheme.typography.caption1,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = state.gsrSensorDescription ?: "Sensor API entry not found",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = when {
                    state.baselineComplete -> "GSR and pulse baseline ready"
                    !state.running -> "Start session for baseline"
                    !state.hasGsrSensor -> "GSR unavailable · pulse ${state.baselineHeartCount}/${state.baselineHeartTarget}"
                    state.baselineGsrCount == 0 -> "Waiting for GSR · 0/${state.baselineTarget} · Pulse ${state.baselineHeartCount}/${state.baselineHeartTarget}"
                    else -> "GSR ${state.baselineGsrCount}/${state.baselineTarget} · Pulse ${state.baselineHeartCount}/${state.baselineHeartTarget}"
                },
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = state.heartRateBpm?.let { "Pulse ${"%.0f".format(it)} bpm" } ?: "Pulse unavailable",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            SignalTrendChart(
                gsrHistory = state.gsrHistory,
                pulseHistory = state.pulseHistory,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        item {
            Text(
                text = "GSR samples: ${state.baselineGsrCount} · Pulse samples: ${state.baselineHeartCount} · Alerts: ${state.alertsRaised}",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center
            )
        }
        item {
            if (state.running) Button(onClick = onStop) { Text("End session") }
            else Button(onClick = onStart) { Text("Start monitoring") }
        }
        item {
            Text(
                text = "Signals show body changes, not their cause. If you feel unwell, tell clinic staff.",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
