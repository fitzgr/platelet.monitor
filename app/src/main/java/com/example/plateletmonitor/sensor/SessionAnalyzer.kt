package com.example.plateletmonitor.sensor

import kotlin.math.pow
import kotlin.math.sqrt

class SessionAnalyzer(
    private val baselineSampleTarget: Int = 120,
    private val stressZThreshold: Double = 1.8,
    private val minStd: Double = 0.1
) {
    private val baselineHeart = mutableListOf<Double>()
    private val baselineGsr = mutableListOf<Double>()

    fun ingestForBaseline(reading: SensorReading): BaselineStats {
        reading.heartRateBpm?.toDouble()?.let { baselineHeart.add(it) }
        reading.gsrMicrosiemens?.toDouble()?.let { baselineGsr.add(it) }
        return currentBaseline()
    }

    fun currentBaseline(): BaselineStats {
        val heartStats = meanStd(baselineHeart)
        val gsrStats = meanStd(baselineGsr)
        val complete = baselineHeart.size >= baselineSampleTarget && baselineGsr.size >= baselineSampleTarget
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
            (it - baseline.heartRateMean) / baseline.heartRateStd.coerceAtLeast(minStd)
        }
        val gsrZ = reading.gsrMicrosiemens?.toDouble()?.let {
            (it - baseline.gsrMean) / baseline.gsrStd.coerceAtLeast(minStd)
        }

        val stressLikely = (hrZ != null && gsrZ != null && hrZ >= stressZThreshold && gsrZ >= stressZThreshold)
        val reason = if (stressLikely) {
            "Both GSR and pulse are elevated from your baseline"
        } else {
            "Within expected range"
        }

        return DeviationResult(
            heartRateZ = hrZ,
            gsrZ = gsrZ,
            stressLikely = stressLikely,
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
