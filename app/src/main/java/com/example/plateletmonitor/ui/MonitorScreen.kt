package com.example.plateletmonitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
                text = state.heartRateBpm?.let { "%.0f".format(it) } ?: "--",
                style = MaterialTheme.typography.display1,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = if (state.hasHeartRateSensor) "BPM" else "Heart-rate sensor unavailable",
                style = MaterialTheme.typography.caption1,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = state.gsrMicrosiemens?.let { "GSR %.2f µS".format(it) }
                    ?: if (state.hasGsrSensor) "GSR waiting for reading" else "GSR not available (optional)",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = if (state.baselineComplete) {
                    "Personal baseline ready"
                } else if (state.running && state.hasHeartRateSensor) {
                    "Baseline ${state.baselineHeartCount}/${state.baselineTarget}"
                } else {
                    "Start a session before donating"
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
                Button(
                    onClick = onStart,
                    enabled = state.hasHeartRateSensor
                ) { Text("Start session") }
            }
        }
        item {
            Text(
                text = "Personal trend cue only. If you feel unwell, tell clinic staff.",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
