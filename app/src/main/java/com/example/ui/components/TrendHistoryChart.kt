package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.InsertChartOutlined
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AirRecord
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendHistoryChart(
    records: List<AirRecord>,
    modifier: Modifier = Modifier
) {
    var selectedRecord by remember { mutableStateOf<AirRecord?>(null) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_history_chart")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Title & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "แนวโน้มสถิติฝุ่น PM2.5 ย้อนหลัง",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = Color(0xFFFF8800), label = "เกณฑ์ไทย 37.5 µg/m³", isDashed = true)
                LegendItem(color = Color(0xFF00B4D8), label = "เกณฑ์ WHO 15 µg/m³", isDashed = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (records.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertChartOutlined,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ยังไม่มีข้อมูลสถิติที่บันทึกไว้",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "สแกนภาพถ่ายหรือเลือกภาพตัวอย่างเพื่อเริ่มบันทึกประวัติ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Take chronological data (earliest to latest) up to 15 points
                val chartPoints = records.take(15).reversed()
                val maxVal = (chartPoints.maxOfOrNull { it.pm25 } ?: 50f).coerceAtLeast(60f)

                // Tooltip info if user selected a point
                if (selectedRecord != null) {
                    val sel = selectedRecord!!
                    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("th", "TH"))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sdf.format(Date(sel.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${sel.province} ${sel.district}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "%.1f µg/m³".format(sel.pm25),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = sel.toAirQualityLevel().color
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(AQI ${sel.aqi})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Interactive Canvas Chart
                val primaryColor = MaterialTheme.colorScheme.primary
                val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                val thaiThresholdColor = Color(0xFFFF8800)
                val whoThresholdColor = Color(0xFF00B4D8)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(chartPoints) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (chartPoints.size - 1).coerceAtLeast(1)
                                val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, chartPoints.size - 1)
                                selectedRecord = chartPoints[tappedIndex]
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 20.dp.toPx()
                    val usableHeight = h - paddingBottom

                    // Draw Horizontal Grid Lines
                    // 1. Thai PCD standard (37.5 µg/m³)
                    val thaiY = usableHeight * (1f - (37.5f / maxVal).coerceIn(0f, 1f))
                    drawLine(
                        color = thaiThresholdColor.copy(alpha = 0.8f),
                        start = Offset(0f, thaiY),
                        end = Offset(w, thaiY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // 2. WHO standard (15 µg/m³)
                    val whoY = usableHeight * (1f - (15.0f / maxVal).coerceIn(0f, 1f))
                    drawLine(
                        color = whoThresholdColor.copy(alpha = 0.8f),
                        start = Offset(0f, whoY),
                        end = Offset(w, whoY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    // Build Line Path and Area Gradient Path
                    val stepX = if (chartPoints.size > 1) w / (chartPoints.size - 1) else w / 2f

                    val linePath = Path()
                    val areaPath = Path()

                    val coordinates = chartPoints.mapIndexed { index, record ->
                        val x = if (chartPoints.size > 1) index * stepX else w / 2f
                        val y = usableHeight * (1f - (record.pm25 / maxVal).coerceIn(0f, 1f))
                        Offset(x, y)
                    }

                    if (coordinates.isNotEmpty()) {
                        linePath.moveTo(coordinates[0].x, coordinates[0].y)
                        areaPath.moveTo(coordinates[0].x, usableHeight)
                        areaPath.lineTo(coordinates[0].x, coordinates[0].y)

                        for (i in 1 until coordinates.size) {
                            val p0 = coordinates[i - 1]
                            val p1 = coordinates[i]
                            val controlX = (p0.x + p1.x) / 2f
                            linePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            areaPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }

                        areaPath.lineTo(coordinates.last().x, usableHeight)
                        areaPath.close()

                        // Draw Area Fill Gradient
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Draw Trend Stroke
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Data Point Circles
                        coordinates.forEachIndexed { idx, point ->
                            val isSelected = selectedRecord == chartPoints[idx]
                            val pointColor = chartPoints[idx].toAirQualityLevel().color

                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = pointColor,
                                radius = if (isSelected) 4.5.dp.toPx() else 2.8.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isDashed) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.5.dp)
                    .background(color, RoundedCornerShape(1.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
