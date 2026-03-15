package com.weighttracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.weighttracker.data.local.WeightRecord
import com.weighttracker.domain.model.*
import com.weighttracker.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeightViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var currentWeight by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showChart by remember { mutableStateOf(false) }
    var showBmiChart by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(7) }

    val timeRanges = listOf(7, 14, 30, 90, 180, 360)
    val timeRangeLabels = mapOf(7 to "7天", 14 to "14天", 30 to "30天", 90 to "90天", 180 to "180天", 360 to "360天")

    val latestRecord = uiState.records.maxByOrNull { it.date }
    val latestBmi = latestRecord?.let { calculateBmi(it.weight, settings.height) }
    val latestCategory = latestBmi?.let { getBmiCategory(it) }

    val statistics = viewModel.getStatistics(uiState.records, settings.height, settings.targetWeight)

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("体重记录") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.List, contentDescription = "历史记录")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "当前体重",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = latestRecord?.let { "%.1f kg".format(it.weight) } ?: "--",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (latestBmi != null) {
                            Text(
                                text = "BMI: %.1f (${latestCategory?.displayName})".format(latestBmi),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "趋势变化",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "较昨日",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (statistics.changeFromYesterday > 0) "+%.1f kg".format(statistics.changeFromYesterday) 
                                           else "%.1f kg".format(statistics.changeFromYesterday),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (statistics.changeFromYesterday > 0) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = "较上周",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (statistics.changeFromLastWeek > 0) "+%.1f kg".format(statistics.changeFromLastWeek) 
                                           else "%.1f kg".format(statistics.changeFromLastWeek),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (statistics.changeFromLastWeek > 0) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text(
                                    text = "目标差距",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (statistics.targetDiff > 0) "+%.1f kg".format(statistics.targetDiff) 
                                           else "%.1f kg".format(statistics.targetDiff),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (statistics.targetDiff > 0) Color.Red else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "30天统计",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("平均", statistics.averageWeight)
                            StatItem("最高", statistics.maxWeight)
                            StatItem("最低", statistics.minWeight)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "体重趋势",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                FilterChip(
                                    selected = showChart && !showBmiChart,
                                    onClick = { showChart = !showChart; showBmiChart = false },
                                    label = { Text("体重") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = showChart && showBmiChart,
                                    onClick = { showChart = !showChart; showBmiChart = true },
                                    label = { Text("BMI") }
                                )
                            }
                        }

                        if (showChart && uiState.recordCount >= 7) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                timeRanges.forEach { range ->
                                    FilterChip(
                                        selected = selectedTimeRange == range,
                                        onClick = { selectedTimeRange = range },
                                        label = { Text(timeRangeLabels[range] ?: "$range", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val endDate = LocalDate.now()
                            val startDate = endDate.minusDays(selectedTimeRange.toLong() - 1)

                            val chartData by viewModel.getChartData(startDate, endDate, settings.height)
                                .collectAsStateWithLifecycle(initialValue = emptyList())

                            if (chartData.isNotEmpty()) {
                                WeightChart(
                                    data = chartData,
                                    showBmi = showBmiChart,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                            }
                        } else if (uiState.recordCount < 7) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "记录超过7天后将显示折线图",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            currentWeight = currentWeight,
            onWeightChange = { currentWeight = it },
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            onDismiss = { showAddDialog = false },
            onConfirm = {
                val weight = currentWeight.toDoubleOrNull()
                if (weight != null && weight > 0) {
                    viewModel.addOrUpdateWeight(weight, selectedDate)
                    currentWeight = ""
                    selectedDate = LocalDate.now()
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (value > 0) "%.1f kg".format(value) else "--",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeightChart(
    data: List<WeightWithBmi>,
    showBmi: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < data.size) {
                                data[index].date.format(DateTimeFormatter.ofPattern("MM/dd"))
                            } else ""
                        }
                    }
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, item ->
                Entry(index.toFloat(), if (showBmi) item.bmi.toFloat() else item.weight.toFloat())
            }

            val dataSet = LineDataSet(entries, if (showBmi) "BMI" else "体重").apply {
                color = android.graphics.Color.parseColor("#4CAF50")
                setCircleColor(android.graphics.Color.parseColor("#4CAF50"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun AddWeightDialog(
    currentWeight: String,
    onWeightChange: (String) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录体重") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentWeight,
                    onValueChange = onWeightChange,
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
                    Text("日期: ${DateUtils.formatFullDate(selectedDate)}")
                    TextButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                onDateChange(LocalDate.of(year, month + 1, day))
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    }) {
                        Text("选择日期")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilledTonalButton(onClick = {
                        val weight = currentWeight.toDoubleOrNull() ?: return@FilledTonalButton
                        if (weight > 0) {
                            onWeightChange((weight + 0.1).toString())
                        }
                    }) {
                        Text("+0.1kg")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(onClick = {
                        val weight = currentWeight.toDoubleOrNull() ?: return@FilledTonalButton
                        if (weight > 0.1) {
                            onWeightChange((weight - 0.1).toString())
                        }
                    }) {
                        Text("-0.1kg")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
