package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreDialog(
    onExportJson: suspend () -> String,
    onRestoreJson: suspend (String) -> Result<Int>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import
    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "สำรองและกู้คืนข้อมูลปลอดภัย",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("สำรองข้อมูล (Export)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("กู้คืนข้อมูล (Import)") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Text(
                        text = "ระบบจะรวบรวมประวัติการตรวจวัดทั้งหมดเข้ารหัสเป็น JSON สำหรับจัดเก็บภายนอกอย่างปลอดภัย",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (exportedJsonText.isEmpty()) {
                        Button(
                            onClick = {
                                scope.launch {
                                    exportedJsonText = onExportJson()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_backup_button")
                        ) {
                            Text("สร้างไฟล์ข้อมูลสำรอง")
                        }
                    } else {
                        OutlinedTextField(
                            value = exportedJsonText,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("รหัสข้อมูลสำรอง JSON") }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AeroScan Backup", exportedJsonText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "คัดลอกข้อมูลสำรองแล้ว", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("คัดลอกไปยังคลิปบอร์ด")
                        }
                    }
                } else {
                    Text(
                        text = "วางรหัสข้อมูลสำรอง JSON ที่คุณเคยส่งออกไว้ เพื่อกู้คืนประวัติและสถิติ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("วางข้อความ JSON ที่นี่...") },
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_json_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val res = onRestoreJson(importJsonText)
                                if (res.isSuccess) {
                                    statusMessage = "กู้คืนข้อมูลสำเร็จ ${res.getOrDefault(0)} รายการ"
                                    Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    statusMessage = "รูปแบบข้อมูลไม่ถูกต้อง กรุณาตรวจสอบรหัส JSON"
                                }
                            }
                        },
                        enabled = importJsonText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_restore_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เริ่มการกู้คืนข้อมูล")
                    }

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ปิด")
            }
        }
    )
}
