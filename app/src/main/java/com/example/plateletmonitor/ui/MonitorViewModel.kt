package com.example.plateletmonitor.ui

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plateletmonitor.data.AppDatabase
import com.example.plateletmonitor.data.SessionEntity
import com.example.plateletmonitor.sensor.BaselineStats
import com.example.plateletmonitor.sensor.SessionAnalyzer
import com.example.plateletmonitor.sensor.SensorCollector
import com.example.plateletmonitor.sensor.SensorReading
import com.example.plateletmonitor.session.AlertManager
import com.example.plateletmonitor.session.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val collector = SensorCollector(application)
    private val analyzer = SessionAnalyzer()
    private val repository = SessionRepository(AppDatabase.getInstance(application).sessionDao())
    private val alerts = AlertManager(application)

    private val _uiState = MutableStateFlow(
        MonitorUiState(
            hasHeartRateSensor = collector.hasHeartRateSensor(),
            hasGsrSensor = collector.hasGsrSensor(),
            gsrSensorDescription = collector.gsrSensorDescription(),
            statusMessage = if (collector.hasGsrSensor()) {
                "Ready to check body signals"
            } else {
                "GSR not exposed to app"
            }
        )
    )
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private var session: SessionEntity? = null
    private var collectionJob: Job? = null
    private var lastAlertMs: Long = 0

    init {
        alerts.createChannelIfNeeded()
    }

    fun startSession() {
        if (collectionJob != null) return

        collectionJob = viewModelScope.launch {
            analyzer.reset()
            lastAlertMs = 0
            val activeSession = repository.startSession()
            session = activeSession
            _uiState.value = MonitorUiState(
                sessionId = activeSession.sessionId,
                running = true,
                hasHeartRateSensor = collector.hasHeartRateSensor(),
                hasGsrSensor = collector.hasGsrSensor(),
                gsrSensorDescription = collector.gsrSensorDescription(),
                statusMessage = if (collector.hasGsrSensor()) {
                    "Learning GSR and pulse baseline"
                } else {
                    "GSR not exposed to app"
                }
            )

            collector.readingsFlow(SensorManager.SENSOR_DELAY_NORMAL).collect { reading ->
                processReading(reading)
            }
        }
    }

    fun stopSession() {
        val activeSession = session ?: return
        collectionJob?.cancel()
        collectionJob = null
        session = null

        _uiState.value = _uiState.value.copy(
            running = false,
            statusMessage = "Session ended",
            changeDetected = false
        )

        viewModelScope.launch {
            repository.endSession(activeSession.sessionId)
        }
    }

    private suspend fun processReading(reading: SensorReading) {
        val activeSession = session ?: return
        val currentState = _uiState.value

        val baseline: BaselineStats = if (!currentState.baselineComplete) {
            analyzer.ingestForBaseline(reading)
        } else {
            analyzer.currentBaseline()
        }

        session = repository.saveBaseline(activeSession, baseline)

        val deviation = if (baseline.complete) {
            analyzer.evaluateDeviation(reading, baseline)
        } else {
            null
        }

        repository.persistReading(
            sessionId = activeSession.sessionId,
            reading = reading.copy(
                heartRateBpm = reading.heartRateBpm.takeIf { reading.heartRateUpdated },
                gsrMicrosiemens = reading.gsrMicrosiemens.takeIf { reading.gsrUpdated }
            ),
            heartRateZ = deviation?.heartRateZ,
            gsrZ = deviation?.gsrZ,
            bodySignalChanged = deviation?.bodySignalChanged ?: false
        )

        var updatedSession = session
        if (deviation?.bodySignalChanged == true) {
            val now = System.currentTimeMillis()
            if (now - lastAlertMs > ALERT_COOLDOWN_MS) {
                alerts.showChangeAlert(
                    activeSession.sessionId,
                    "${deviation.reason}. If you feel unwell, tell clinic staff."
                )
                lastAlertMs = now
                updatedSession = updatedSession?.let { repository.incrementAlert(it) }
                session = updatedSession
            }
        }

        _uiState.value = _uiState.value.copy(
            baselineComplete = baseline.complete,
            baselineHeartCount = baseline.heartRateCount,
            baselineGsrCount = baseline.gsrCount,
            heartRateBpm = reading.heartRateBpm,
            gsrMicrosiemens = reading.gsrMicrosiemens,
            heartRateZ = deviation?.heartRateZ ?: currentState.heartRateZ,
            gsrZ = deviation?.gsrZ ?: currentState.gsrZ,
            changeDetected = deviation?.bodySignalChanged ?: false,
            alertsRaised = updatedSession?.alertCount ?: 0,
            statusMessage = when {
                !currentState.hasGsrSensor -> "GSR not exposed to app"
                !baseline.complete -> "Learning GSR and pulse baseline"
                deviation?.bodySignalChanged == true -> "Body signal changed"
                else -> "Monitoring GSR and pulse"
            }
        )
    }

    companion object {
        private const val ALERT_COOLDOWN_MS = 60_000L
    }
}
