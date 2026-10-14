package com.example.plateletmonitor.ui

data class SignalPoint(
    val timestampEpochMs: Long,
    val value: Float
)

data class MonitorUiState(
    val sessionId: String? = null,
    val running: Boolean = false,
    val hasHeartRateSensor: Boolean = false,
    val hasGsrSensor: Boolean = false,
    val gsrSensorDescription: String? = null,
    val baselineComplete: Boolean = false,
    val baselineHeartCount: Int = 0,
    val baselineGsrCount: Int = 0,
    val baselineHeartTarget: Int = 30,
    val baselineTarget: Int = 120,
    val heartRateBpm: Float? = null,
    val gsrMicrosiemens: Float? = null,
    val gsrHistory: List<SignalPoint> = emptyList(),
    val pulseHistory: List<SignalPoint> = emptyList(),
    val heartRateZ: Double? = null,
    val gsrZ: Double? = null,
    val changeDetected: Boolean = false,
    val alertsRaised: Int = 0,
    val statusMessage: String = "Ready"
)
