package com.example.plateletmonitor.ui

data class MonitorUiState(
    val sessionId: String? = null,
    val running: Boolean = false,
    val hasHeartRateSensor: Boolean = false,
    val hasGsrSensor: Boolean = false,
    val baselineComplete: Boolean = false,
    val baselineHeartCount: Int = 0,
    val baselineGsrCount: Int = 0,
    val baselineTarget: Int = 120,
    val heartRateBpm: Float? = null,
    val gsrMicrosiemens: Float? = null,
    val heartRateZ: Double? = null,
    val gsrZ: Double? = null,
    val stressLikely: Boolean = false,
    val alertsRaised: Int = 0,
    val statusMessage: String = "Ready"
)
