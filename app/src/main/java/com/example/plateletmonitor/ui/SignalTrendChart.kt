package com.example.plateletmonitor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlin.math.abs

private val GsrChartColor = Color(0xFF5EEAD4)
private val PulseChartColor = Color(0xFFFFB74D)
private const val CHART_WINDOW_MS = 5 * 60 * 1000L

@Composable
fun SignalTrendChart(
    gsrHistory: List<SignalPoint>,
    pulseHistory: List<SignalPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("Recent trend · 5 min", style = MaterialTheme.typography.caption2)
        Canvas(Modifier.fillMaxWidth().height(48.dp)) {
            val latest = maxOf(
                gsrHistory.lastOrNull()?.timestampEpochMs ?: 0L,
                pulseHistory.lastOrNull()?.timestampEpochMs ?: 0L
            )
            val left = 4.dp.toPx()
            val right = size.width - 4.dp.toPx()
            val top = 4.dp.toPx()
            val bottom = size.height - 4.dp.toPx()
            for (i in 0..2) {
                val y = top + (bottom - top) * i / 2f
                drawLine(
                    color = Color(0x557F8C98),
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            if (latest > 0L) {
                drawSeries(gsrHistory, GsrChartColor, latest, left, right, top, bottom)
                drawSeries(pulseHistory, PulseChartColor, latest, left, right, top, bottom)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("● GSR", color = GsrChartColor, style = MaterialTheme.typography.caption2)
            Text("● Pulse", color = PulseChartColor, style = MaterialTheme.typography.caption2)
        }
        Text(
            text = when {
                gsrHistory.isEmpty() && pulseHistory.isEmpty() -> "Waiting for sensor readings"
                gsrHistory.isEmpty() -> "No live GSR samples received"
                else -> "Each line uses its own vertical scale"
            },
            style = MaterialTheme.typography.caption2,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawSeries(
    points: List<SignalPoint>,
    color: Color,
    latestTimestamp: Long,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float
) {
    val windowStart = latestTimestamp - CHART_WINDOW_MS
    val visible = points.filter { it.timestampEpochMs in windowStart..latestTimestamp }
        .sortedBy { it.timestampEpochMs }
    if (visible.isEmpty()) return

    val minValue = visible.minOf { it.value }
    val maxValue = visible.maxOf { it.value }
    val spread = maxValue - minValue
    val padding = if (spread == 0f) maxOf(abs(minValue) * 0.1f, 1f) else spread * 0.12f
    val lower = minValue - padding
    val valueRange = (maxValue + padding - lower).coerceAtLeast(0.001f)
    val coordinates = visible.map { point ->
        val timeFraction = ((point.timestampEpochMs - windowStart).toFloat() / CHART_WINDOW_MS)
            .coerceIn(0f, 1f)
        val valueFraction = ((point.value - lower) / valueRange).coerceIn(0f, 1f)
        Offset(
            left + (right - left) * timeFraction,
            bottom - (bottom - top) * valueFraction
        )
    }

    if (coordinates.size == 1) {
        drawCircle(color, 3.dp.toPx(), coordinates.first())
        return
    }
    for (i in 1 until coordinates.size) {
        drawLine(color, coordinates[i - 1], coordinates[i], strokeWidth = 2.dp.toPx())
    }
}
