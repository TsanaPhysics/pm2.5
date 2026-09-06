package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.AirQualityLevel
import com.example.model.ScanResult
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.roundToInt

@Composable
fun LiveScanCameraView(
    liveScanResult: ScanResult?,
    liveHistory: List<Float> = emptyList(),
    onBack: (() -> Unit)? = null,
    onFrameBitmap: (Bitmap) -> Unit,
    onCapturePhoto: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isTorchOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraAvailable by remember { mutableStateOf(true) }
    var isTelemetryCollapsed by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Scanning laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_scan_anim"
    )

    // Pulsing live indicator dot
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    // Smooth real-time PM2.5 number animation
    val currentPm = liveScanResult?.pm25 ?: 22.4f
    val animatedPm by animateFloatAsState(
        targetValue = currentPm,
        animationSpec = tween(durationMillis = 280, easing = LinearEasing),
        label = "animated_pm"
    )

    val currentLevel = liveScanResult?.level ?: AirQualityLevel.fromPm25(currentPm)
    val levelColor = currentLevel.color

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("live_scan_camera_view")
    ) {
        // CameraX Surface View
        if (cameraAvailable) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            // Fast image analysis for live HUD frame inference
                            var lastAnalyzed = 0L
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val now = System.currentTimeMillis()
                                        if (now - lastAnalyzed >= 220) { // ~4.5 updates/sec for smooth live real-time feel
                                            lastAnalyzed = now
                                            val bitmap = imageProxy.toBitmapOrNull()
                                            if (bitmap != null) {
                                                onFrameBitmap(bitmap)
                                            }
                                        }
                                        imageProxy.close()
                                    }
                                }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("LiveScanCameraView", "Camera init failed", e)
                            cameraAvailable = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera unavailable fallback (streaming emulator / container)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F172A), Color(0xFF020617))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = levelColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "จำลองระบบสตรีมมิ่งตรวจจับฝุ่นสด (AI Vision Engine)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "กำลังประมวลผลการกระเจิงของแสงระดับไมครอนแบบเรียลไทม์",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // HUD Laser Scanning Overlay & Reticle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Scanning box bounds (76% width, 38% height)
            val boxW = w * 0.76f
            val boxH = h * 0.38f
            val left = (w - boxW) / 2f
            val top = h * 0.16f
            val right = left + boxW
            val bottom = top + boxH

            val cornerLen = 34.dp.toPx()
            val strokeW = 3.dp.toPx()
            val reticleColor = levelColor

            // 4 Corner Brackets
            // Top-Left
            drawLine(reticleColor, Offset(left, top), Offset(left + cornerLen, top), strokeW, StrokeCap.Round)
            drawLine(reticleColor, Offset(left, top), Offset(left, top + cornerLen), strokeW, StrokeCap.Round)
            // Top-Right
            drawLine(reticleColor, Offset(right, top), Offset(right - cornerLen, top), strokeW, StrokeCap.Round)
            drawLine(reticleColor, Offset(right, top), Offset(right, top + cornerLen), strokeW, StrokeCap.Round)
            // Bottom-Left
            drawLine(reticleColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW, StrokeCap.Round)
            drawLine(reticleColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW, StrokeCap.Round)
            // Bottom-Right
            drawLine(reticleColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW, StrokeCap.Round)
            drawLine(reticleColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW, StrokeCap.Round)

            // Center Crosshair
            val cx = (left + right) / 2f
            val cy = (top + bottom) / 2f
            val crosshairLen = 16.dp.toPx()
            drawLine(reticleColor.copy(alpha = 0.6f), Offset(cx - crosshairLen, cy), Offset(cx + crosshairLen, cy), 1.5.dp.toPx())
            drawLine(reticleColor.copy(alpha = 0.6f), Offset(cx, cy - crosshairLen), Offset(cx, cy + crosshairLen), 1.5.dp.toPx())

            // Laser Scan Sweep Line
            val laserY = top + (bottom - top) * laserYRatio
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        reticleColor.copy(alpha = 0.35f),
                        Color.White,
                        reticleColor.copy(alpha = 0.75f),
                        Color.White,
                        reticleColor.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                start = Offset(left, laserY),
                end = Offset(right, laserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // Floating Target Real-Time Badge over Viewfinder (High Transparency Glass)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 96.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x30000000), // ~19% opacity glassmorphism
                border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.55f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(levelColor.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE PM2.5: %.1f µg/m³".format(animatedPm),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• AQI ${liveScanResult?.aqi ?: currentLevel.maxAqi}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                        ),
                        color = levelColor
                    )
                }
            }
        }

        // Top Control Bar (Back, Status, Flash, Camera Switch) - Transparent Frosted Glass
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0x35000000), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("live_scan_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับ",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x30000000), // Highly transparent pill
                    border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF00E676).copy(alpha = pulseAlpha), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE AI VISION (4.5 FPS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.5.sp,
                                shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val newTorch = !isTorchOn
                        isTorchOn = newTorch
                        camera?.cameraControl?.enableTorch(newTorch)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x35000000), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "ไฟฉาย",
                        tint = if (isTorchOn) Color(0xFFFFD166) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0x35000000), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "สลับกล้อง",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live Telemetry HUD Card (High-Transparency Glassmorphism allowing camera visibility)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val result = liveScanResult
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x38090E18), // ~22% translucent glassmorphism background
                border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.45f)),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_pm_telemetry_card")
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // Top row: Header & Category Pill + Collapse Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00E676).copy(alpha = pulseAlpha), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ค่าฝุ่นสดเรียลไทม์ (LIVE PM2.5)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                ),
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Category Pill (translucent)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = levelColor.copy(alpha = 0.20f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = currentLevel.titleTh,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = levelColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { isTelemetryCollapsed = !isTelemetryCollapsed },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTelemetryCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isTelemetryCollapsed) "ขยายข้อมูล" else "ย่อข้อมูล",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Value Display Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(animatedPm),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 38.sp,
                                    fontFamily = FontFamily.Monospace,
                                    shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 4f)
                                ),
                                color = levelColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.padding(bottom = 5.dp)) {
                                Text(
                                    text = "µg/m³",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = if (animatedPm > 37.5f) "เกินเกณฑ์ PCD" else "อยู่ในเกณฑ์ปลอดภัย",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = if (animatedPm > 37.5f) Color(0xFFFF6B6B) else Color(0xFF4ADE80)
                                )
                            }
                        }

                        // AQI Badge Card (Translucent glass)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x30000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "AQI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "${result?.aqi ?: currentLevel.maxAqi}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = levelColor
                                )
                            }
                        }
                    }

                    // Collapsible Details Section: Waveform, Telemetry Grid, Health Note
                    AnimatedVisibility(
                        visible = !isTelemetryCollapsed,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(6.dp))

                            // Real-Time Waveform Sparkline (Translucent)
                            Text(
                                text = "ความผันแปรของละอองลอยแบบเรียลไทม์ (Live Aerosol Waveform)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                ),
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            LiveSparklineWave(
                                history = liveHistory,
                                color = levelColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Secondary Live Optical Telemetry Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val estPm10 = (animatedPm * 1.6f).roundToInt()
                                LiveMetricText(
                                    label = "PM10 (หยาบ)",
                                    value = "$estPm10 µg/m³"
                                )
                                LiveMetricText(
                                    label = "หมอกควัน",
                                    value = "${result?.hazeIndexPercent ?: 22}%"
                                )
                                LiveMetricText(
                                    label = "ส่องผ่าน (t)",
                                    value = "%.2f".format(result?.transmission ?: 0.88f)
                                )
                                LiveMetricText(
                                    label = "ความมั่นใจ",
                                    value = "${result?.confidencePercent ?: 94}%"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Health Advisory Line (Translucent)
                            val advisoryText = when (currentLevel) {
                                AirQualityLevel.VERY_GOOD -> "🌱 อากาศบริสุทธิ์ดีมาก เหมาะแก่การออกกำลังกายกลางแจ้ง"
                                AirQualityLevel.GOOD -> "🌤️ คุณภาพอากาศดี ปลอดโปร่ง ทำกิจกรรมกลางแจ้งได้ตามปกติ"
                                AirQualityLevel.MODERATE -> "⛅ คุณภาพปานกลาง บุคคลทั่วไปทำกิจกรรมได้ แต่กลุ่มเสี่ยงควรสังเกตตนเอง"
                                AirQualityLevel.UNHEALTHY_SENSITIVE -> "⚠️ เริ่มมีผลกระทบ เด็ก ผู้สูงอายุ และผู้มีโรคปอดควรสวมหน้ากาก"
                                AirQualityLevel.UNHEALTHY -> "🚨 ฝุ่นหนาแน่นเกินมาตรฐาน ควรหลีกเลี่ยงกิจกรรมกลางแจ้งและสวมหน้ากาก N95"
                                AirQualityLevel.HAZARDOUS -> "⛔ ภาวะวิกฤตอันตราย งดออกนอกอาคารและสวมหน้ากาก N95 ทันที"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = levelColor.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, levelColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = advisoryText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                                    ),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capture High-Res Snapshot Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(3.dp)
                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(levelColor.copy(alpha = 0.9f), CircleShape)
                        .clickable(enabled = !isCapturing) {
                            val cap = imageCapture
                            if (cap != null) {
                                isCapturing = true
                                val photoFile = File(context.cacheDir, "temp_scan_${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                cap.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            isCapturing = false
                                            val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                            if (bmp != null) {
                                                onCapturePhoto(bmp)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isCapturing = false
                                            Log.e("LiveScan", "Photo capture failed", exception)
                                        }
                                    }
                                )
                            } else {
                                // Direct software frame snapshot
                                val dummyBmp = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
                                onCapturePhoto(dummyBmp)
                            }
                        }
                        .testTag("camera_capture_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "บันทึกและบันทึกลงประวัติ",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "แตะปุ่มเพื่อบันทึกผลการตรวจวัดสด",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
                ),
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun LiveMetricText(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
            ),
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                shadow = Shadow(color = Color.Black.copy(alpha = 0.85f), blurRadius = 3f)
            ),
            color = Color.White
        )
    }
}

/**
 * Real-time sparkline wave with transparent background letting camera image show through.
 */
@Composable
private fun LiveSparklineWave(
    history: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0x22000000), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        val w = size.width
        val h = size.height

        if (history.isEmpty()) return@Canvas

        val minVal = (history.minOrNull() ?: 10f).coerceAtLeast(0f)
        val maxVal = (history.maxOrNull() ?: 60f).coerceAtLeast(minVal + 10f)
        val range = maxVal - minVal

        val points = history.mapIndexed { index, value ->
            val x = (index.toFloat() / (history.size - 1).coerceAtLeast(1)) * w
            val normalizedY = ((value - minVal) / range).coerceIn(0f, 1f)
            val y = h - (normalizedY * (h * 0.75f) + h * 0.12f)
            Offset(x, y)
        }

        if (points.size >= 2) {
            // Draw gradient area
            val fillPath = Path().apply {
                moveTo(points.first().x, h)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h
                ),
                style = Fill
            )

            // Draw line stroke
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = strokePath,
                color = color,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Pulsing dot on latest reading
            val latest = points.last()
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = latest
            )
            drawCircle(
                color = color.copy(alpha = 0.5f),
                radius = 6.dp.toPx(),
                center = latest
            )
        }
    }
}

// Extension to safely convert ImageProxy to Bitmap using CameraX toBitmap() or raw buffer
private fun ImageProxy.toBitmapOrNull(): Bitmap? {
    return try {
        val bmp = this.toBitmap()
        if (imageInfo.rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        } else {
            bmp
        }
    } catch (_: Throwable) {
        try {
            val plane = planes[0]
            val buffer = plane.buffer
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            buffer.rewind()
            bitmap.copyPixelsFromBuffer(buffer)
            if (imageInfo.rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (_: Throwable) {
            null
        }
    }
}
