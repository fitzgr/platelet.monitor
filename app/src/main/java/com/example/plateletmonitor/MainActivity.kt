package com.example.plateletmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Scaffold
import com.example.plateletmonitor.ui.MonitorScreen
import com.example.plateletmonitor.ui.MonitorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val vm: MonitorViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()

                var hasPermissions by remember { mutableStateOf(checkRequiredPermissions()) }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    hasPermissions = checkRequiredPermissions()
                }

                LaunchedEffect(Unit) {
                    if (!hasPermissions) {
                        launcher.launch(requiredPermissions())
                    }
                }

                Scaffold(timeText = { TimeText() }) {
                    if (hasPermissions) {
                        MonitorScreen(
                            state = state,
                            onStart = vm::startSession,
                            onStop = vm::stopSession
                        )
                    } else {
                        Text("Sensor and notification permissions are required")
                    }
                }
            }
        }
    }

    private fun requiredPermissions(): Array<String> {
        val items = mutableListOf(Manifest.permission.BODY_SENSORS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            items += Manifest.permission.POST_NOTIFICATIONS
            items += Manifest.permission.BODY_SENSORS_BACKGROUND
        }
        return items.toTypedArray()
    }

    private fun checkRequiredPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
