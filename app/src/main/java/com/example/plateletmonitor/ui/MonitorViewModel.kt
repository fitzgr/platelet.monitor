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
            hasGsrSensor = collector.hasGsrSensor()
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

        viewModelScope.launch {
            session = repository.startSession()
            _uiState.value = _uiState.value.copy(
                running = true,
                sessionId = session?.sessionId,
                statusMessage = "Learning baseline"
            )
        }

        collectionJob = viewModelScope.launch {
            collector.readingsFlow(SensorManager.SENSOR_DELAY_NORMAL).collect { reading ->
                processReading(reading)
            }
        }
    }

    fun stopSession() {
        val activeSession = session ?: return
        collectionJob?.cancel()
        collectionJob = null

        viewModelScope.launch {
            repository.endSession(activeSession.sessionId)
            _uiState.value = _uiState.value.copy(
                running = false,
                statusMessage = "Session ended"
            )
            session = null
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
            reading = reading,
            heartRateZ = deviation?.heartRateZ,
            gsrZ = deviation?.gsrZ,
            stressLikely = deviation?.stressLikely ?: false
        )

        var updatedSession = session
        if (deviation?.stressLikely == true) {
            val now = System.currentTimeMillis()
            if (now - lastAlertMs > ALERT_COOLDOWN_MS) {
                alerts.showStressAlert(
                    activeSession.sessionId,
                    "Possible stress response. Notify medical staff now."
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
            heartRateZ = deviation?.heartRateZ,
            gsrZ = deviation?.gsrZ,
            stressLikely = deviation?.stressLikely ?: false,
            alertsRaised = updatedSession?.alertCount ?: 0,
            statusMessage = when {
                !baseline.complete -> "Learning baseline"
                deviation?.stressLikely == true -> "Stress pattern detected"
                else -> "Monitoring"
            }
        )
    }

    companion object {
        private const val ALERT_COOLDOWN_MS = 60_000L
    }
}
