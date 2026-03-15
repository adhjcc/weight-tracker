package com.weighttracker.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.weighttracker.data.local.WeightRecord
import com.weighttracker.domain.model.calculateBmi
import com.weighttracker.domain.model.getBmiCategory
import com.weighttracker.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: WeightViewModel,
    height: Float,
    onNavigateBack: () -> Unit,
    onExportCsv: (String) -> Unit,
    onImportCsv: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<WeightRecord?>(null) }
    var editWeight by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf(LocalDate.now()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingRecord by remember { mutableStateOf<WeightRecord?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "导入")
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "导出")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    val bmi = calculateBmi(record.weight, height)
                    val category = getBmiCategory(bmi)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = DateUtils.formatFullDate(record.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "%.1f kg".format(record.weight),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "BMI: %.1f (%s)".format(bmi, category.displayName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(onClick = {
                                    editingRecord = record
                                    editWeight = record.weight.toString()
                                    editDate = record.date
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                                }
                                IconButton(onClick = {
                                    deletingRecord = record
                                    showDeleteConfirm = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showEditDialog && editingRecord != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑记录") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("体重 (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("日期: ${DateUtils.formatFullDate(editDate)}")
                        TextButton(onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    editDate = LocalDate.of(year, month + 1, day)
                                },
                                editDate.year,
                                editDate.monthValue - 1,
                                editDate.dayOfMonth
                            ).show()
                        }) {
                            Text("选择日期")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val weight = editWeight.toDoubleOrNull()
                    if (weight != null && weight > 0) {
                        viewModel.updateRecord(editingRecord!!.copy(weight = weight, date = editDate))
                        showEditDialog = false
                        editingRecord = null
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteConfirm && deletingRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${DateUtils.formatFullDate(deletingRecord!!.date)} 的记录吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(deletingRecord!!)
                    showDeleteConfirm = false
                    deletingRecord = null
                }) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出数据") },
            text = { Text("确定要导出所有记录为CSV格式吗？") },
            confirmButton = {
                TextButton(onClick = {
                    val csv = viewModel.exportToCsv(uiState.records)
                    onExportCsv(csv)
                    showExportDialog = false
                }) {
                    Text("导出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showImportDialog) {
        var csvContent by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入数据") },
            text = {
                Column {
                    Text("请粘贴CSV内容（格式：日期,体重,备注）")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvContent,
                        onValueChange = { csvContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("日期,体重,备注\n2024-01-01,70.0,") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (csvContent.isNotBlank()) {
                        onImportCsv(csvContent)
                        showImportDialog = false
                    }
                }) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
