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
                text = state.gsrMicrosiemens?.let { "%.2f".format(it) } ?: "--",
                style = MaterialTheme.typography.display1,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = if (state.hasGsrSensor) "GSR / cEDA" else "GSR not exposed to this app",
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
                text = if (state.baselineComplete) {
                    "GSR baseline ready"
                } else if (state.running && state.hasGsrSensor) {
                    "GSR baseline ${state.baselineGsrCount}/${state.baselineTarget}"
                } else if (state.running) {
                    "Waiting for GSR samples"
                } else {
                    "Start a session to check GSR"
                },
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = state.heartRateBpm?.let { "HR ${"%.0f".format(it)} bpm · context" }
                    ?: "Heart rate unavailable",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = when {
                    !state.hasMotionSensor -> "Motion sensor unavailable"
                    state.movementDetected == null -> "Motion: waiting for sample"
                    state.movementDetected -> "Motion detected · context"
                    else -> "Low motion · context"
                },
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = "GSR samples: ${state.baselineGsrCount} · Alerts: ${state.alertsRaised}",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center
            )
        }
        item {
            if (state.running) {
                Button(onClick = onStop) { Text("End session") }
            } else {
                Button(onClick = onStart) { Text("Start GSR session") }
            }
        }
        item {
            Text(
                text = "GSR is a nonspecific trend. If you feel unwell, tell clinic staff.",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
