package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Masks
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AirQualityLevel
import com.example.model.ScanResult
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoSurface

@Composable
fun HealthAdvisoryCard(
    scanResult: ScanResult,
    modifier: Modifier = Modifier
) {
    val level = scanResult.level

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("health_advisory_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = level.color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ข้อแนะนำการดูแลสุขภาพ",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Exceeded Standard Warning Banner if applicable
            if (scanResult.isAlertTriggered(37.5f)) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = level.color.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, level.color.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "คำเตือน",
                            tint = level.color,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ค่าฝุ่น PM2.5 เกินเกณฑ์มาตรฐานประเทศไทย (37.5 µg/m³) โปรดปฏิบัติตามคำแนะนำอย่างเคร่งครัด",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = level.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Guidance Items
            AdvisoryRowItem(
                icon = Icons.Default.Masks,
                label = "การสวมหน้ากาก",
                value = level.maskRecommendation,
                iconTint = level.color
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdvisoryRowItem(
                icon = Icons.Default.DirectionsRun,
                label = "กิจกรรมกลางแจ้ง",
                value = level.outdoorRecommendation,
                iconTint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdvisoryRowItem(
                icon = Icons.Default.Face,
                label = "คำเตือนกลุ่มเสี่ยง",
                value = level.sensitiveGroupWarning,
                iconTint = if (level.requiresAlert) level.color else MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdvisoryRowItem(
                icon = Icons.Default.Air,
                label = "เครื่องฟอกอากาศ",
                value = level.airPurifierAdvice,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            // Atmospheric & AI Model Metrics Row
            Text(
                text = "ผลวิเคราะห์ความหนาแน่นเชิงแสง (AI Vision Optics)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    title = "ดัชนีหมอกควัน",
                    value = "${scanResult.hazeIndexPercent}%",
                    tint = level.color
                )
                MetricPill(
                    title = "การส่องผ่านแสง (t)",
                    value = "%.2f".format(scanResult.transmission),
                    tint = MaterialTheme.colorScheme.primary
                )
                MetricPill(
                    title = "สัมประสิทธิ์ β",
                    value = "%.2f km⁻¹".format(scanResult.extinctionCoeff),
                    tint = MaterialTheme.colorScheme.secondary
                )
                MetricPill(
                    title = "ความแม่นยำ AI",
                    value = "${scanResult.confidencePercent}%",
                    tint = Color(0xFF06D6A0)
                )
            }
        }
    }
}

@Composable
private fun AdvisoryRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = tint
            )
        }
    }
}
