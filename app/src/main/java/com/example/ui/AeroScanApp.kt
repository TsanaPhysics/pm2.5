package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AreaSelectionSheet
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LiveScanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoPrimaryLight
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AirViewModel

enum class MainTab(val titleEn: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Home", Icons.Default.Dashboard),
    HISTORY("History", Icons.Default.ShowChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AeroScanApp(
    viewModel: AirViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var isLiveScanOpen by remember { mutableStateOf(false) }
    var isAreaSheetOpen by remember { mutableStateOf(false) }

    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isGpsAuto by viewModel.isGpsAuto.collectAsStateWithLifecycle()

    // Permissions requesting launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fineLocationGranted = results[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = results[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.detectGpsLocation()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    // Biometric Lock Screen Overlay
    if (isBiometricEnabled && !isAppUnlocked) {
        BiometricLockOverlay(
            onAuthenticateClicked = {
                val activity = context as? FragmentActivity
                if (activity != null && viewModel.biometricManager.isBiometricAvailable()) {
                    viewModel.biometricManager.authenticate(
                        activity = activity,
                        title = "ยืนยันตัวตนความปลอดภัย",
                        onSuccess = { viewModel.setAppUnlocked(true) },
                        onError = { /* Keep locked */ }
                    )
                } else {
                    viewModel.setAppUnlocked(true)
                }
            }
        )
        return
    }

    // Live Camera Mode Fullscreen View
    if (isLiveScanOpen) {
        LiveScanScreen(
            viewModel = viewModel,
            onBack = { isLiveScanOpen = false }
        )
        return
    }

    // Bento Grid App Shell
    Scaffold(
        containerColor = BentoBg,
        topBar = {
            Surface(
                color = BentoBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Header Info (matching Bento HTML)
                    Column(
                        modifier = Modifier
                            .clickable { isAreaSheetOpen = true }
                            .testTag("top_bar_location_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isGpsAuto) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                contentDescription = "เลือกพื้นที่",
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${currentLocation.district}, ${currentLocation.province}".uppercase(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Slate500
                            )
                        }
                        Text(
                            text = "${"%.4f".format(currentLocation.latitude)}° N, ${"%.4f".format(currentLocation.longitude)}° E",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Slate400,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }

                    // Top Action Badges (Bento styled circular pills)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cloud sync badge
                        Surface(
                            shape = CircleShape,
                            color = BentoSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    viewModel.syncNow { _, _ -> }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Status",
                                    tint = Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Biometric security badge
                        Surface(
                            shape = CircleShape,
                            color = BentoPrimaryLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.25f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { selectedTab = MainTab.SETTINGS }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Lock",
                                    tint = BentoPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Bento Dock matching Design HTML: nav with elevated center camera button
            Surface(
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    BentoDockItem(
                        icon = Icons.Default.Dashboard,
                        label = "Home",
                        selected = selectedTab == MainTab.DASHBOARD,
                        onClick = { selectedTab = MainTab.DASHBOARD },
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    // History
                    BentoDockItem(
                        icon = Icons.Default.ShowChart,
                        label = "History",
                        selected = selectedTab == MainTab.HISTORY,
                        onClick = { selectedTab = MainTab.HISTORY },
                        modifier = Modifier.testTag("nav_tab_history")
                    )

                    // Center Elevated Live AI Camera Button
                    Surface(
                        onClick = { isLiveScanOpen = true },
                        shape = RoundedCornerShape(18.dp),
                        color = BentoPrimary,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("fab_live_scan")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "สแกนสด AI",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Settings
                    BentoDockItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        selected = selectedTab == MainTab.SETTINGS,
                        onClick = { selectedTab = MainTab.SETTINGS },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToLiveScan = { isLiveScanOpen = true },
                        onNavigateToHistory = { selectedTab = MainTab.HISTORY },
                        onOpenAreaSelector = { isAreaSheetOpen = true }
                    )
                }
                MainTab.HISTORY -> {
                    HistoryScreen(viewModel = viewModel)
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Area Selection Sheet
    if (isAreaSheetOpen) {
        AreaSelectionSheet(
            currentLocation = currentLocation,
            isGpsAuto = isGpsAuto,
            onSelectLocation = { loc ->
                viewModel.setLocation(loc)
            },
            onEnableGpsAuto = {
                viewModel.toggleGpsAuto(true)
            },
            onSetCustomCoordinates = { province, district, subDistrict, lat, lng ->
                viewModel.setCustomCoordinates(province, district, subDistrict, lat, lng)
            },
            onDismiss = { isAreaSheetOpen = false }
        )
    }
}

@Composable
private fun BentoDockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) BentoPrimary else Slate400,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) BentoPrimary else Slate400
        )
    }
}
