package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LiveScanCameraView
import com.example.ui.viewmodel.AirViewModel

@Composable
fun LiveScanScreen(
    viewModel: AirViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveResult by viewModel.liveScanResult.collectAsStateWithLifecycle()
    val liveHistory by viewModel.liveHistoryList.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startLiveScan()
        onDispose {
            viewModel.stopLiveScan()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("live_scan_screen")
    ) {
        LiveScanCameraView(
            liveScanResult = liveResult,
            liveHistory = liveHistory,
            onBack = onBack,
            onFrameBitmap = { bitmap ->
                viewModel.processLiveFrame(bitmap)
            },
            onCapturePhoto = { capturedBitmap ->
                viewModel.saveLiveSnapshot(capturedBitmap)
                onBack()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
