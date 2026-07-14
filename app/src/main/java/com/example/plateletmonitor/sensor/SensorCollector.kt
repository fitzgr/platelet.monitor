package com.example.plateletmonitor.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorCollector(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun hasHeartRateSensor(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null

    fun hasGsrSensor(): Boolean = findGsrSensor() != null

    fun readingsFlow(sampleRateMicros: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<SensorReading> = callbackFlow {
        val heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val gsrSensor = findGsrSensor()

        var latestHeartRate: Float? = null
        var latestGsr: Float? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_HEART_RATE -> {
                        latestHeartRate = event.values.firstOrNull()
                    }
                    else -> {
                        if (isLikelyGsrSensor(event.sensor)) {
                            latestGsr = event.values.firstOrNull()
                        }
                    }
                }

                trySend(
                    SensorReading(
                        timestampEpochMs = System.currentTimeMillis(),
                        heartRateBpm = latestHeartRate,
                        gsrMicrosiemens = latestGsr
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
}
