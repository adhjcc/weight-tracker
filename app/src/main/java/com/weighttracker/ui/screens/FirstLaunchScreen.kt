package com.weighttracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.weighttracker.data.local.SettingsDataStore
import kotlinx.coroutines.launch

@Composable
fun FirstLaunchScreen(
    settingsDataStore: SettingsDataStore,
    onComplete: () -> Unit
) {
    var height by remember { mutableStateOf("170") }
    var targetWeight by remember { mutableStateOf("70") }
    var step by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (step == 1) {
                Text(
                    text = "欢迎使用体重记录",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "让我们先设置您的身高",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("身高 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val h = height.toFloatOrNull()
                        if (h != null && h > 0) {
                            step = 2
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("下一步")
                }
            } else {
                Text(
                    text = "设置目标体重",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "设置您的目标体重",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it },
                    label = { Text("目标体重 (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val h = height.toFloatOrNull()
                        val tw = targetWeight.toFloatOrNull()
                        if (h != null && h > 0 && tw != null && tw > 0) {
                            scope.launch {
                                settingsDataStore.updateHeight(h)
                                settingsDataStore.updateTargetWeight(tw)
                                settingsDataStore.setFirstLaunchComplete()
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("完成")
                }
            }
        }
    }
}
