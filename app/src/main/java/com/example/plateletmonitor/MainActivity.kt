package com.example.plateletmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.plateletmonitor.ui.MonitorScreen
import com.example.plateletmonitor.ui.MonitorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val vm: MonitorViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()

                var hasSensorPermission by remember { mutableStateOf(checkSensorPermission()) }
                val sensorPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    hasSensorPermission = checkSensorPermission()
                }
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (!hasSensorPermission) {
                        sensorPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
                    }
                }
                LaunchedEffect(hasSensorPermission) {
                    if (hasSensorPermission &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !hasNotificationPermission()
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Scaffold(timeText = { TimeText() }) {
                    if (hasSensorPermission) {
                        MonitorScreen(
                            state = state,
                            onStart = vm::startSession,
                            onStop = vm::stopSession
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Heart-rate access is needed to monitor a session.",
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    sensorPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
                                }
                            ) {
                                Text("Grant access")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkSensorPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}
