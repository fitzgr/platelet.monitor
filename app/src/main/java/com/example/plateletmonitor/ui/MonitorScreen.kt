package com.example.plateletmonitor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun MonitorScreen(
    state: MonitorUiState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )
        Text(text = "Session: ${state.sessionId?.take(8) ?: "-"}")
        Text(text = "HR: ${state.heartRateBpm?.let { "%.1f bpm".format(it) } ?: "n/a"}")
        Text(text = "GSR: ${state.gsrMicrosiemens?.let { "%.2f uS".format(it) } ?: "n/a"}")
        Text(text = "HR z: ${state.heartRateZ?.let { "%.2f".format(it) } ?: "-"}")
        Text(text = "GSR z: ${state.gsrZ?.let { "%.2f".format(it) } ?: "-"}")
        Text(text = "Alerts: ${state.alertsRaised}")
        Text(
            text = "Baseline ${state.baselineHeartCount}/${state.baselineTarget} HR, ${state.baselineGsrCount}/${state.baselineTarget} GSR",
            textAlign = TextAlign.Center
        )
        Text(text = "Sensors: HR ${if (state.hasHeartRateSensor) "ok" else "missing"}, GSR ${if (state.hasGsrSensor) "ok" else "missing"}")

        if (!state.running) {
            Button(onClick = onStart) {
                Text("Start")
            }
        } else {
            Button(onClick = onStop) {
                Text("Stop")
            }
        }
    }
}
