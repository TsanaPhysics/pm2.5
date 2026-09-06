package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AirQualityLevel

@Composable
fun AirQualityGauge(
    pm25: Float,
    aqi: Int,
    level: AirQualityLevel,
    modifier: Modifier = Modifier
) {
    // Max scale 150 µg/m³ for visual sweep
    val normalizedProgress = (pm25 / 150.0f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedProgress,
        animationSpec = tween(durationMillis = 900),
        label = "pm25_gauge_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("air_quality_gauge"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            val levelColor = level.color
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            Canvas(modifier = Modifier.size(230.dp)) {
                val strokeWidth = 18.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                // 240-degree arc from 150 to 390 degrees
                val startAngle = 150f
                val sweepAngle = 240f

                // Background Track
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Progress Arc with Gradient
                val activeSweep = sweepAngle * animatedProgress
                if (activeSweep > 1f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF00B4D8),
                                Color(0xFF06D6A0),
                                Color(0xFFFFD166),
                                Color(0xFFFF8800),
                                Color(0xFFE63946)
                            )
                        ),
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Threshold Indicator Tick for Thai PCD Limit (37.5 µg/m³)
                // 37.5 / 150 = 0.25 -> angle = 150 + 240 * 0.25 = 210 degrees
            }

            // Center Display Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PM2.5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "%.1f".format(pm25),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 44.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "µg/m³",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // AQI Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = levelColor.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, levelColor)
                ) {
                    Text(
                        text = "AQI $aqi",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Level Title Badge & Status Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = level.color.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, level.color),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(level.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = level.titleTh,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = level.color
                )
                if (level.requiresAlert) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "คำเตือนเกินมาตรฐาน",
                        tint = level.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
