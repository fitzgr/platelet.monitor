package com.example.plateletmonitor.sensor

data class SensorReading(
    val timestampEpochMs: Long,
    val heartRateBpm: Float?,
    val gsrMicrosiemens: Float?
)

data class BaselineStats(
    val heartRateCount: Int,
    val gsrCount: Int,
    val heartRateMean: Double,
    val heartRateStd: Double,
    val gsrMean: Double,
    val gsrStd: Double,
    val complete: Boolean
)

data class DeviationResult(
    val heartRateZ: Double?,
    val gsrZ: Double?,
    val stressLikely: Boolean,
    val reason: String
)
