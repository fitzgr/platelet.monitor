package com.example.plateletmonitor.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class SensorCollector(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun hasHeartRateSensor(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null

    fun hasMotionSensor(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null

    fun hasGsrSensor(): Boolean = findGsrSensor() != null

    fun gsrSensorDescription(): String? = findGsrSensor()?.let { sensor ->
        "${sensor.name} · ${sensor.stringType ?: "custom sensor"}"
    }

    fun readingsFlow(sampleRateMicros: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<SensorReading> = callbackFlow {
        val heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val gsrSensor = findGsrSensor()
        val motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        var latestHeartRate: Float? = null
        var latestGsr: Float? = null
        var latestMovement: Boolean? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                var heartRateUpdated = false
                var gsrUpdated = false
                var motionUpdated = false

                when {
                    event.sensor.type == Sensor.TYPE_HEART_RATE -> {
                        latestHeartRate = event.values.firstOrNull()
                        heartRateUpdated = true
                    }
                    isLikelyGsrSensor(event.sensor) -> {
                        latestGsr = event.values.firstOrNull()
                        gsrUpdated = true
                    }
                    event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION -> {
                        val x = event.values.getOrNull(0) ?: 0f
                        val y = event.values.getOrNull(1) ?: 0f
                        val z = event.values.getOrNull(2) ?: 0f
                        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                        latestMovement = acceleration >= MOTION_THRESHOLD_METERS_PER_SECOND_SQUARED
                        motionUpdated = true
                    }
                }

                trySend(
                    SensorReading(
                        timestampEpochMs = System.currentTimeMillis(),
                        heartRateBpm = latestHeartRate,
                        gsrMicrosiemens = latestGsr,
                        heartRateUpdated = heartRateUpdated,
                        gsrUpdated = gsrUpdated,
                        movementDetected = latestMovement,
                        motionUpdated = motionUpdated
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (heartSensor != null) {
            sensorManager.registerListener(listener, heartSensor, sampleRateMicros)
        }
        if (gsrSensor != null) {
            sensorManager.registerListener(listener, gsrSensor, sampleRateMicros)
        }
        if (motionSensor != null) {
            sensorManager.registerListener(listener, motionSensor, sampleRateMicros)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    private fun findGsrSensor(): Sensor? {
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        return allSensors.firstOrNull { isLikelyGsrSensor(it) }
    }

    private fun isLikelyGsrSensor(sensor: Sensor): Boolean {
        val type = sensor.stringType?.lowercase().orEmpty()
        val name = sensor.name.lowercase()
        return type.contains("gsr") ||
            type.contains("skin_conductance") ||
            type.contains("electrodermal") ||
            type.contains("eda") ||
            name.contains("gsr") ||
            name.contains("skin conductance") ||
            name.contains("electrodermal")
    }

    companion object {
        private const val MOTION_THRESHOLD_METERS_PER_SECOND_SQUARED = 0.5f
    }
}
