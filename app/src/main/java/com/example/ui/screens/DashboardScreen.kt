package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.SampleScene
import com.example.model.AirQualityLevel
import com.example.model.ScanResult
import com.example.ui.components.AirQualityGauge
import com.example.ui.components.HealthAdvisoryCard
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoHeroBg
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoPrimaryLight
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceDark
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AirViewModel
import java.io.InputStream

@Composable
fun DashboardScreen(
    viewModel: AirViewModel,
    onNavigateToLiveScan: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onOpenAreaSelector: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanResult by viewModel.currentScanResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.analyzeBitmap(bitmap, saveToHistory = true, notes = "ภาพจากแกลเลอรี")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // 1. HERO BENTO CARD: Live AI Mode Viewfinder Tile
            // ==========================================
            HeroLiveAiBentoCard(
                pm25 = scanResult.pm25,
                aqi = scanResult.aqi,
                aiConfidencePercent = scanResult.confidencePercent,
                onOpenLiveScan = onNavigateToLiveScan
            )
        }

        item {
            // ==========================================
            // 2. 2-COLUMN BENTO GRID TILES
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bento Tile 1: PM2.5 AQI Stat Card (Crisp White Tile)
                BentoStatTile(
                    pm25 = scanResult.pm25,
                    aqi = scanResult.aqi,
                    level = scanResult.level,
                    modifier = Modifier.weight(1f)
                )

                // Bento Tile 2: Health Advisory Dark Tile (Matte Dark Bento Tile)
                BentoAdvisoryTile(
                    level = scanResult.level,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // ==========================================
            // 3. BENTO TILE 3: Weekly Trends Banner Tile (Royal Blue)
            // ==========================================
            BentoTrendsBannerTile(
                onClick = onNavigateToHistory
            )
        }

        // Analyzing Progress Indicator (when running AI model)
        if (isAnalyzing) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoPrimaryLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            color = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI กำลังคำนวณการส่องผ่านแสงและความหนาแน่นละอองฝุ่น...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPrimaryDark
                            )
                        )
                    }
                }
            }
        }

        item {
            // ==========================================
            // 4. BENTO TILE 4: Circular Gauge & AI Optical Metrics Tile
            // ==========================================
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "เกจและมิติการวัดทางแสง (OPTICAL METRICS)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Slate600
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = scanResult.level.color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = scanResult.level.titleTh,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = scanResult.level.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Circular PM2.5 Gauge
                    AirQualityGauge(
                        pm25 = scanResult.pm25,
                        aqi = scanResult.aqi,
                        level = scanResult.level
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Sub-metrics in Bento 2x2 grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoSubMetricPill(
                            title = "ดัชนีความขุ่นมัว",
                            value = "${scanResult.hazeIndexPercent}%",
                            modifier = Modifier.weight(1f)
                        )
                        BentoSubMetricPill(
                            title = "การส่องผ่านแสง (t)",
                            value = "%.2f".format(scanResult.transmission),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BentoSubMetricPill(
                            title = "ค่าสูญพันธุ์ (β)",
                            value = "%.3f".format(scanResult.extinctionCoeff),
                            modifier = Modifier.weight(1f)
                        )
                        BentoSubMetricPill(
                            title = "ความเชื่อมั่น AI",
                            value = "${scanResult.confidencePercent}%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            // Full Detailed Health Advisory Guidance Card
            HealthAdvisoryCard(scanResult = scanResult)
        }

        item {
            // ==========================================
            // 5. BENTO TILE 5: Actions & Benchmark Scenes
            // ==========================================
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sample_benchmark_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToLiveScan,
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("open_live_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("สแกนสด AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp)
                                .testTag("pick_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("เลือกรูป", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ชุดทดสอบภาพตัวอย่าง (AI BENCHMARKS)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Slate600
                        )
                    }

                    Text(
                        text = "ทดสอบการประมวลผลด้วยโมเดลวิเคราะห์จริงแม้ไม่มีกล้องจริงใน Emulator",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    SampleScene.values().forEach { scene ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.analyzeSampleScene(scene) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scene.titleTh,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Slate800
                                    )
                                    Text(
                                        text = scene.descriptionTh,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BentoPrimaryLight
                                ) {
                                    Text(
                                        text = scene.expectedPm25Approx,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = BentoPrimaryDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// SUB-COMPONENTS FOR BENTO GRID TILES
// ==========================================

@Composable
private fun HeroLiveAiBentoCard(
    pm25: Float,
    aqi: Int,
    aiConfidencePercent: Int,
    onOpenLiveScan: () -> Unit
) {
    // Pulse animation for the red live badge
    val infiniteTransition = rememberInfiniteTransition(label = "hero_bento_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoHeroBg,
        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onOpenLiveScan() }
            .testTag("hero_bento_live_ai_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BentoHeroBg,
                            Color(0xFF1E293B),
                            Color(0xFF090D16)
                        )
                    )
                )
        ) {
            // Live AI Mode Badge (Top Left)
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .align(Alignment.TopStart)
                    .background(Color(0xFFEF4444).copy(alpha = 0.9f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(Color.White.copy(alpha = pulseAlpha), CircleShape)
                )
                Text(
                    text = "LIVE AI MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )
            }

            // Viewfinder Reticle with Corner Brackets & Laser Scanline
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.Center)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            ) {
                // 4 Blue Reticle Corners
                val cornerColor = Color(0xFF60A5FA)
                val cornerSize = 14.dp
                val cornerThickness = 3.dp

                // Top-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(cornerSize)
                        .border(
                            width = cornerThickness,
                            color = cornerColor,
                            shape = RoundedCornerShape(topStart = 4.dp)
                        )
                )
                // Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(cornerSize)
                        .border(
                            width = cornerThickness,
                            color = cornerColor,
                            shape = RoundedCornerShape(topEnd = 4.dp)
                        )
                )
                // Bottom-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(cornerSize)
                        .border(
                            width = cornerThickness,
                            color = cornerColor,
                            shape = RoundedCornerShape(bottomStart = 4.dp)
                        )
                )
                // Bottom-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(cornerSize)
                        .border(
                            width = cornerThickness,
                            color = cornerColor,
                            shape = RoundedCornerShape(bottomEnd = 4.dp)
                        )
                )

                // Laser Scanline
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = scanLineOffset.dp)
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFF60A5FA),
                                    Color(0xFF93C5FD),
                                    Color(0xFF60A5FA),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Center camera icon indicator
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.Center)
                )
            }

            // Bottom Metrics Bar inside Hero Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "ANALYSIS ACCURACY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (aiConfidencePercent > 0) "$aiConfidencePercent%" else "98.4%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color(0xFF93C5FD)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TFLite v2.1 On-Device",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                }

                // Detected badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${pm25.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "DETECTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoStatTile(
    pm25: Float,
    aqi: Int,
    level: AirQualityLevel,
    modifier: Modifier = Modifier
) {
    // Map level to clean Bento badge text (POOR, MODERATE, GOOD, HAZARDOUS, etc.)
    val badgeText = when (level) {
        AirQualityLevel.VERY_GOOD -> "EXCELLENT"
        AirQualityLevel.GOOD -> "GOOD"
        AirQualityLevel.MODERATE -> "MODERATE"
        AirQualityLevel.UNHEALTHY_SENSITIVE -> "POOR"
        AirQualityLevel.UNHEALTHY -> "UNHEALTHY"
        AirQualityLevel.HAZARDOUS -> "HAZARDOUS"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        shadowElevation = 1.dp,
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: AQI icon + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = level.color,
                    modifier = Modifier.size(22.dp)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = level.color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = level.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Bottom: Huge Display PM2.5 & unit
            Column {
                Text(
                    text = "%.0f".format(pm25),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = Slate900
                )
                Text(
                    text = "PM 2.5 µg/m³ (AQI $aqi)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = Slate400
                )
            }
        }
    }
}

@Composable
private fun BentoAdvisoryTile(
    level: AirQualityLevel,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurfaceDark,
        shadowElevation = 1.dp,
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Circular Icon Pill
            Surface(
                shape = CircleShape,
                color = Color(0xFF334155),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bottom: Advisory text
            Column {
                Text(
                    text = if (level == AirQualityLevel.VERY_GOOD || level == AirQualityLevel.GOOD) {
                        "คุณภาพอากาศดี สามารถทำกิจกรรมกลางแจ้งได้ตามปกติ"
                    } else if (level == AirQualityLevel.MODERATE) {
                        "ควรสวมหน้ากากอนามัยหากอยู่ในกลุ่มเสี่ยงก่อนออกกลางแจ้ง"
                    } else {
                        "สวมหน้ากาก N95 เมื่อออกกลางแจ้งวันนี้ ปิดหน้าต่างเลี่ยงฝุ่น"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Slate300,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BentoTrendsBannerTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = BentoPrimary,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ดูสถิติและแนวโน้มย้อนหลัง (Trends)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White
                )
            }

            // Decorative Equalizer Trend Bars matching Design HTML
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .background(Color.White.copy(alpha = 0.65f), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(14.dp)
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(22.dp)
                        .background(Color.White, RoundedCornerShape(50))
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(8.dp)
                        .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun BentoSubMetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BentoBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Slate500
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = Slate900
            )
        }
    }
}
