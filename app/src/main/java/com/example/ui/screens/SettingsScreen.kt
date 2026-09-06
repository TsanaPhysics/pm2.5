package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.AirViewModel

@Composable
fun SettingsScreen(
    viewModel: AirViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alertThreshold by viewModel.alertThreshold.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

    var showBackupDialog by remember { mutableStateOf(false) }

    if (showBackupDialog) {
        BackupRestoreDialog(
            onExportJson = { viewModel.exportBackupJson() },
            onRestoreJson = { json -> viewModel.restoreBackupJson(json) },
            onDismiss = { showBackupDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "การตั้งค่าและความปลอดภัย",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Slate900
            )
            Text(
                text = "กำหนดการแจ้งเตือนเตือนภัย, สำรองข้อมูล และระบบไบโอเมตริกซ์",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Security & Biometric
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ความปลอดภัยและข้อมูลส่วนบุคคล",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ล็อกอินผ่านไบโอเมตริกซ์ (Biometric Lock)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                            Text(
                                text = "ต้องสแกนลายนิ้วมือหรือใบหน้าก่อนเข้าถึงประวัติข้อมูลสุขภาพส่วนตัว",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }

                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val activity = context as? FragmentActivity
                                    if (activity != null && viewModel.biometricManager.isBiometricAvailable()) {
                                        viewModel.biometricManager.authenticate(
                                            activity = activity,
                                            title = "เปิดใช้งานความปลอดภัยไบโอเมตริกซ์",
                                            onSuccess = {
                                                viewModel.setBiometricEnabled(true)
                                                Toast.makeText(context, "เปิดใช้งานไบโอเมตริกซ์เรียบร้อย", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        // Fallback toggle for devices/emulators without enrolled fingerprint
                                        viewModel.setBiometricEnabled(true)
                                        Toast.makeText(context, "เปิดระบบป้องกันข้อมูลส่วนบุคคล", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewModel.setBiometricEnabled(false)
                                }
                            },
                            modifier = Modifier.testTag("biometric_toggle")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Alert Notification Threshold
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFFF8800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "การแจ้งเตือนค่าฝุ่นเกินมาตรฐาน",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ระบบจะส่ง Push Notification แจ้งเตือนทันทีที่พบความผิดปกติของอากาศเกินเกณฑ์ที่คุณกำหนด",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "เกณฑ์แจ้งเตือนปัจจุบัน:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate700
                        )
                        Text(
                            text = "%.1f µg/m³".format(alertThreshold),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFFFF8800)
                        )
                    }

                    Slider(
                        value = alertThreshold,
                        onValueChange = { viewModel.setAlertThreshold(it) },
                        valueRange = 15.0f..100.0f,
                        steps = 16,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "15 (WHO)", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Text(text = "37.5 (มาตรฐานไทย)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Slate700)
                        Text(text = "100 (วิกฤต)", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Cloud Sync & Backup Data
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "การเชื่อมต่อคลาวด์และสำรองข้อมูล",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ระบบรองรับการทำงานออฟไลน์เต็มรูปแบบ และทำการซิงโครไนซ์ขึ้นคลาวด์อัตโนมัติเมื่อตรวจพบสัญญาณอินเทอร์เน็ต",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncNow { success, count ->
                                    if (success) {
                                        Toast.makeText(context, "ซิงค์ข้อมูลกับคลาวด์เรียบร้อย ($count รายการ)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "โหมดออฟไลน์: บันทึกข้อมูลปลอดภัยในเครื่อง", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("sync_now_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ซิงค์เดี๋ยวนี้", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showBackupDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_backup_dialog_button")
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("สำรอง / กู้คืน", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: AI Model & Privacy Declaration
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "มาตรฐานเทคโนโลยี On-Device AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "โมเดลวิเคราะห์ความหนาแน่นละอองฝุ่น PM2.5 (AeroOptical Prior & Mie Scattering) ทำงานบนโปรเซสเซอร์ของสมาร์ทโฟนโดยตรง ไม่มีการส่งภาพถ่ายส่วนตัวออกนอกเครื่อง จึงรับประกันความเป็นส่วนตัวและความรวดเร็ว แม้ไม่มีอินเทอร์เน็ต",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "เวอร์ชันแอปพลิเคชัน: 1.0.0 (AeroScan Production Bento Edition)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
