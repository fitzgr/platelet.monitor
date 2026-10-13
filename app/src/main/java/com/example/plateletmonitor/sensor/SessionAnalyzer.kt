package com.example.plateletmonitor.sensor

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Compares readings with a baseline learned for one procedure session.
 *
 * GSR/cEDA is the required signal for a ready state. Heart rate is retained
 * as context and is not used to infer a calcium level or a citrate reaction.
 */
class SessionAnalyzer(
    private val baselineSampleTarget: Int = 120,
    private val changeZThreshold: Double = 1.8,
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
        if (reading.heartRateUpdated) {
            reading.heartRateBpm?.toDouble()?.let { baselineHeart.add(it) }
        }
        if (reading.gsrUpdated) {
            reading.gsrMicrosiemens?.toDouble()?.let { baselineGsr.add(it) }
        }
        return currentBaseline()
    }

    fun currentBaseline(): BaselineStats {
        val heartStats = meanStd(baselineHeart)
        val gsrStats = meanStd(baselineGsr)
        val complete = baselineGsr.size >= baselineSampleTarget
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
        val hrZ = if (reading.heartRateUpdated) {
            reading.heartRateBpm?.toDouble()?.let {
                (it - baseline.heartRateMean) / baseline.heartRateStd.coerceAtLeast(minHeartRateStd)
            }
        } else {
            null
        }
        val gsrZ = if (reading.gsrUpdated) {
            reading.gsrMicrosiemens?.toDouble()?.let {
                if (baseline.gsrCount > 0) {
                    (it - baseline.gsrMean) / baseline.gsrStd.coerceAtLeast(minGsrStd)
                } else {
                    null
                }
            }
        } else {
            null
        }

        // cEDA/GSR changes are nonspecific; this is a trend cue, not a reaction detector.
        val changeDetected = gsrZ != null && gsrZ >= changeZThreshold
        val reason = if (changeDetected) {
            "GSR is elevated from this session's baseline"
        } else {
            "No marked GSR change from this session's baseline"
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
