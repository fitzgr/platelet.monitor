package com.example.plateletmonitor.session

import com.example.plateletmonitor.data.SensorSampleEntity
import com.example.plateletmonitor.data.SessionDao
import com.example.plateletmonitor.data.SessionEntity
import com.example.plateletmonitor.sensor.BaselineStats
import com.example.plateletmonitor.sensor.SensorReading
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val dao: SessionDao) {
    fun observeSessions(): Flow<List<SessionEntity>> = dao.observeSessions()

    suspend fun startSession(): SessionEntity {
        val entity = SessionEntity(
            sessionId = UUID.randomUUID().toString(),
            startedAtEpochMs = System.currentTimeMillis(),
            endedAtEpochMs = null,
            baselineComplete = false,
            baselineHeartRateMean = 0.0,
            baselineHeartRateStd = 1.0,
            baselineGsrMean = 0.0,
            baselineGsrStd = 1.0,
            alertCount = 0,
            notes = null
        )
        dao.upsertSession(entity)
        return entity
    }

    suspend fun saveBaseline(session: SessionEntity, baseline: BaselineStats): SessionEntity {
        val updated = session.copy(
            baselineComplete = baseline.complete,
            baselineHeartRateMean = baseline.heartRateMean,
            baselineHeartRateStd = baseline.heartRateStd,
            baselineGsrMean = baseline.gsrMean,
            baselineGsrStd = baseline.gsrStd
        )
        dao.upsertSession(updated)
        return updated
    }

    suspend fun incrementAlert(session: SessionEntity): SessionEntity {
        val updated = session.copy(alertCount = session.alertCount + 1)
        dao.upsertSession(updated)
        return updated
    }

    suspend fun persistReading(
        sessionId: String,
        reading: SensorReading,
        heartRateZ: Double?,
        gsrZ: Double?,
        stressLikely: Boolean
    ) {
        dao.insertSample(
            SensorSampleEntity(
                sessionId = sessionId,
                timestampEpochMs = reading.timestampEpochMs,
                heartRateBpm = reading.heartRateBpm,
                gsrMicrosiemens = reading.gsrMicrosiemens,
                heartRateZ = heartRateZ,
                gsrZ = gsrZ,
                stressLikely = stressLikely
            )
        )
    }

    suspend fun endSession(sessionId: String) {
        dao.endSession(sessionId, System.currentTimeMillis())
    }
}
