package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AreaLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaSelectionSheet(
    currentLocation: AreaLocation,
    isGpsAuto: Boolean,
    onSelectLocation: (AreaLocation) -> Unit,
    onEnableGpsAuto: () -> Unit,
    onSetCustomCoordinates: (province: String, district: String, subDistrict: String, lat: Double, lng: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Presets / Search, 1: Custom Lat/Long
    var searchQuery by remember { mutableStateOf("") }

    // Custom Lat/Long fields
    var customProvince by remember { mutableStateOf(currentLocation.province) }
    var customDistrict by remember { mutableStateOf(currentLocation.district) }
    var customSubDistrict by remember { mutableStateOf(currentLocation.subDistrict) }
    var customLat by remember { mutableStateOf(currentLocation.latitude.toString()) }
    var customLng by remember { mutableStateOf(currentLocation.longitude.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("area_selection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "เลือกพื้นที่ตรวจวัดสภาพอากาศ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "กำหนดตำแหน่งเพื่อวิเคราะห์สภาพอากาศเฉพาะจุด",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "ปิด")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action: Enable GPS Auto Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGpsAuto) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isGpsAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onEnableGpsAuto()
                        onDismiss()
                    }
                    .testTag("enable_gps_auto_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isGpsAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (isGpsAuto) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ใช้ตำแหน่ง GPS อัตโนมัติ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ดึงพิกัดดาวเทียมของสมาร์ทโฟนตามเวลาจริง",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isGpsAuto) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "กำลังใช้งาน",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("เลือกจังหวัด / อำเภอ / ตำบล") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ระบุพิกัด Lat / Long") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("ค้นหาจังหวัด อำเภอ หรือตำบล") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("area_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filtered = AreaLocation.PRESET_LOCATIONS.filter {
                    it.province.contains(searchQuery, ignoreCase = true) ||
                    it.district.contains(searchQuery, ignoreCase = true) ||
                    it.subDistrict.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(filtered) { loc ->
                        val isSelected = !isGpsAuto && loc.province == currentLocation.province &&
                                loc.district == currentLocation.district && loc.subDistrict == currentLocation.subDistrict

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectLocation(loc)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${loc.province} - ${loc.district} (${loc.subDistrict})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "พิกัด: ${loc.getCoordinatesFormatted()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "เลือกอยู่",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    }
                }
            } else {
                // Custom Lat/Long Coordinates Form
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customProvince,
                            onValueChange = { customProvince = it },
                            label = { Text("จังหวัด") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customDistrict,
                            onValueChange = { customDistrict = it },
                            label = { Text("อำเภอ/เขต") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customSubDistrict,
                        onValueChange = { customSubDistrict = it },
                        label = { Text("ตำบล/แขวง") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("ละติจูด (Latitude)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customLng,
                            onValueChange = { customLng = it },
                            label = { Text("ลองจิจูด (Longitude)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val lat = customLat.toDoubleOrNull() ?: currentLocation.latitude
                            val lng = customLng.toDoubleOrNull() ?: currentLocation.longitude
                            onSetCustomCoordinates(
                                customProvince,
                                customDistrict,
                                customSubDistrict,
                                lat,
                                lng
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("apply_custom_coordinates_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.EditLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ใช้พิกัดที่กำหนดเอง")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
