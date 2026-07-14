package com.example.plateletmonitor.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sensor_samples",
    indices = [Index(value = ["sessionId", "timestampEpochMs"])]
)
data class SensorSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestampEpochMs: Long,
    val heartRateBpm: Float?,
    val gsrMicrosiemens: Float?,
    val heartRateZ: Double?,
    val gsrZ: Double?,
    val stressLikely: Boolean
)
