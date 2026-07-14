package com.example.plateletmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val baselineComplete: Boolean,
    val baselineHeartRateMean: Double,
    val baselineHeartRateStd: Double,
    val baselineGsrMean: Double,
    val baselineGsrStd: Double,
    val alertCount: Int,
    val notes: String?
)
