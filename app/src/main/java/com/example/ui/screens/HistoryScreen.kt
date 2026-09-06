package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AirRecord
import com.example.ui.components.TrendHistoryChart
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.AirViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: AirViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val avgPm25 by viewModel.averagePm25.collectAsStateWithLifecycle()
    val maxPm25 by viewModel.maxPm25.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val exceededCount by viewModel.exceededCount.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("ล้างประวัติการตรวจวัดทั้งหมด?") },
            text = { Text("ข้อมูลที่บันทึกไว้ในฐานข้อมูลเครื่องจะถูกลบทั้งหมดและไม่สามารถกู้คืนได้ (เว้นแต่สำรองข้อมูลไว้)") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("ล้างทั้งหมด", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg)
            .padding(horizontal = 16.dp)
            .testTag("history_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Header & Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "สถิติและประวัติย้อนหลัง",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "วิเคราะห์แนวโน้มสุขภาพและการสัมผัสมลพิษระยะยาว",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (records.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ล้างประวัติ",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 KPI Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "ค่าเฉลี่ย PM2.5",
                    value = if (avgPm25 != null) "%.1f".format(avgPm25) else "--",
                    unit = "µg/m³",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "ค่าสูงสุดที่พบ",
                    value = if (maxPm25 != null) "%.1f".format(maxPm25) else "--",
                    unit = "µg/m³",
                    tint = if ((maxPm25 ?: 0f) > 37.5f) Color(0xFFE63946) else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "จำนวนครั้งที่วัด",
                    value = "$totalCount",
                    unit = "ครั้ง",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "เกินมาตรฐาน",
                    value = "$exceededCount",
                    unit = "ครั้ง (>37.5)",
                    tint = if (exceededCount > 0) Color(0xFFFF8800) else Color(0xFF06D6A0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Trend Bezier Curve Chart
            TrendHistoryChart(records = records)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "รายการบันทึกล่าสุด (${records.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (records.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ไม่มีข้อมูลประวัติในขณะนี้",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(records, key = { it.id }) { record ->
                HistoryRecordItem(
                    record = record,
                    onDelete = { viewModel.deleteRecord(record.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    unit: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Slate500
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = tint
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate400,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryRecordItem(
    record: AirRecord,
    onDelete: () -> Unit
) {
    val level = record.toAirQualityLevel()
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("th", "TH"))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level indicator badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(level.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "%.0f".format(record.pm25),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = level.color
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${record.province}, ${record.district}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    if (record.pm25 > 37.5f) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = level.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${sdf.format(Date(record.timestamp))} • AQI ${record.aqi} • ${level.titleTh}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )

                if (!record.notes.isNullOrBlank()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "ลบรายการ",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
