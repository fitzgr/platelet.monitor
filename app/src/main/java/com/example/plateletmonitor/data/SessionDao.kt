package com.example.plateletmonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Insert
    suspend fun insertSample(sample: SensorSampleEntity)

    @Query("SELECT * FROM sessions ORDER BY startedAtEpochMs DESC")
    fun observeSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sensor_samples WHERE sessionId = :sessionId ORDER BY timestampEpochMs ASC")
    fun observeSamplesForSession(sessionId: String): Flow<List<SensorSampleEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Query("UPDATE sessions SET endedAtEpochMs = :endedAtEpochMs WHERE sessionId = :sessionId")
    suspend fun endSession(sessionId: String, endedAtEpochMs: Long)
}
