package com.weighttracker.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.weighttracker.data.local.UserSettings
import com.weighttracker.data.local.SettingsDataStore
import com.weighttracker.receiver.ReminderScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WeightViewModel,
    settings: UserSettings,
    settingsDataStore: SettingsDataStore,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var height by remember(settings.height) { mutableStateOf(settings.height.toString()) }
    var targetWeight by remember(settings.targetWeight) { mutableStateOf(settings.targetWeight.toString()) }
    var reminderEnabled by remember(settings.reminderEnabled) { mutableStateOf(settings.reminderEnabled) }
    var reminderHour by remember(settings.reminderHour) { mutableStateOf(settings.reminderHour) }
    var reminderMinute by remember(settings.reminderMinute) { mutableStateOf(settings.reminderMinute) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "身体数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("身高 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = targetWeight,
                            onValueChange = { targetWeight = it },
                            label = { Text("目标体重 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val h = height.toFloatOrNull()
                                val tw = targetWeight.toFloatOrNull()
                                scope.launch {
                                    if (h != null && h > 0) {
                                        settingsDataStore.updateHeight(h)
                                    }
                                    if (tw != null && tw > 0) {
                                        settingsDataStore.updateTargetWeight(tw)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("保存")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "提醒设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("每日提醒")
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasPermission) {
                                                reminderEnabled = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        } else {
                                            reminderEnabled = true
                                        }
                                    } else {
                                        reminderEnabled = false
                                    }

                                    scope.launch {
                                        settingsDataStore.updateReminderEnabled(reminderEnabled)
                                        if (reminderEnabled) {
                                            ReminderScheduler.scheduleReminder(context, reminderHour, reminderMinute)
                                        } else {
                                            ReminderScheduler.cancelReminder(context)
                                        }
                                    }
                                }
                            )
                        }

                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                reminderHour = hour
                                                reminderMinute = minute
                                                scope.launch {
                                                    settingsDataStore.updateReminderTime(hour, minute)
                                                    ReminderScheduler.scheduleReminder(context, hour, minute)
                                                }
                                            },
                                            reminderHour,
                                            reminderMinute,
                                            true
                                        ).show()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("提醒时间")
                                Text(
                                    text = String.format("%02d:%02d", reminderHour, reminderMinute),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "体重记录 v1.0",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "一款简洁的体重记录APP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
