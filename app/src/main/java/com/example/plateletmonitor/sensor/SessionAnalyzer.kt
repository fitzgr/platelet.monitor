package com.example.plateletmonitor.sensor

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Compares readings with a baseline learned for one procedure session.
 *
 * GSR is optional because Wear OS devices do not share a standard GSR sensor.
 * A session can become ready from heart-rate samples alone.
 */
class SessionAnalyzer(
    private val baselineSampleTarget: Int = 120,
    private val stressZThreshold: Double = 1.8,
    private val minHeartRateStd: Double = 3.0,
    private val minGsrStd: Double = 0.1
) {
    private val baselineHeart = mutableListOf<Double>()
    private val baselineGsr = mutableListOf<Double>()

    fun reset() {
        baselineHeart.clear()
        baselineGsr.clear()
    }

    fun ingestForBaseline(reading: SensorReading): BaselineStats {
        reading.heartRateBpm?.toDouble()?.let { baselineHeart.add(it) }
        reading.gsrMicrosiemens?.toDouble()?.let { baselineGsr.add(it) }
        return currentBaseline()
    }

    fun currentBaseline(): BaselineStats {
        val heartStats = meanStd(baselineHeart)
        val gsrStats = meanStd(baselineGsr)
        // Heart rate is the required signal. GSR is an optional corroborating signal.
        val complete = baselineHeart.size >= baselineSampleTarget
        return BaselineStats(
            heartRateCount = baselineHeart.size,
            gsrCount = baselineGsr.size,
            heartRateMean = heartStats.first,
            heartRateStd = heartStats.second,
            gsrMean = gsrStats.first,
            gsrStd = gsrStats.second,
            complete = complete
        )
    }

    fun evaluateDeviation(reading: SensorReading, baseline: BaselineStats): DeviationResult {
        val hrZ = reading.heartRateBpm?.toDouble()?.let {
            (it - baseline.heartRateMean) / baseline.heartRateStd.coerceAtLeast(minHeartRateStd)
        }
        val gsrZ = reading.gsrMicrosiemens?.toDouble()?.let {
            if (baseline.gsrCount > 0) {
                (it - baseline.gsrMean) / baseline.gsrStd.coerceAtLeast(minGsrStd)
            } else {
                null
            }
        }

        // This is a personal-baseline cue, not a clinical or vasovagal-event detector.
        // If GSR is available it must also be elevated; otherwise the cue uses heart rate alone.
        val changeDetected = hrZ != null &&
            hrZ >= stressZThreshold &&
            (gsrZ == null || gsrZ >= stressZThreshold)
        val reason = if (changeDetected) {
            "Your pulse is elevated from this session's baseline"
        } else {
            "No marked change from this session's baseline"
        }

        return DeviationResult(
            heartRateZ = hrZ,
            gsrZ = gsrZ,
            stressLikely = changeDetected,
            reason = reason
        )
    }

    private fun meanStd(values: List<Double>): Pair<Double, Double> {
        if (values.isEmpty()) {
            return 0.0 to 1.0
        }
        val mean = values.sum() / values.size
        val variance = values.sumOf { (it - mean).pow(2) } / values.size
        return mean to sqrt(variance)
    }
}
