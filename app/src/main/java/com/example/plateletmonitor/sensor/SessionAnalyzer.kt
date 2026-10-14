package com.example.plateletmonitor.sensor

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Compares GSR and pulse with baselines learned for one procedure session.
 * It reports signal changes without assigning a medical cause.
 */
class SessionAnalyzer(
    private val baselineSampleTarget: Int = 120,
    private val heartRateBaselineTarget: Int = 30,
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
        val complete = baselineGsr.size >= baselineSampleTarget &&
            baselineHeart.size >= heartRateBaselineTarget
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

        val changedSignals = buildList {
            if (gsrZ != null && abs(gsrZ) >= changeZThreshold) add("GSR")
            if (hrZ != null && abs(hrZ) >= changeZThreshold) add("pulse")
        }
        val reason = if (changedSignals.isNotEmpty()) {
            "${changedSignals.joinToString(" and ")} changed from this session's baseline"
        } else {
            "No marked GSR or pulse change from this session's baseline"
        }

        return DeviationResult(
            heartRateZ = hrZ,
            gsrZ = gsrZ,
            bodySignalChanged = changedSignals.isNotEmpty(),
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
