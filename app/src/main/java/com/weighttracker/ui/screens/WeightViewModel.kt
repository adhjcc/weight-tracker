package com.weighttracker.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.weighttracker.data.local.UserSettings
import com.weighttracker.data.local.WeightRecord
import com.weighttracker.data.local.SettingsDataStore
import com.weighttracker.data.repository.WeightRepository
import com.weighttracker.domain.model.Statistics
import com.weighttracker.domain.model.WeightWithBmi
import com.weighttracker.domain.model.calculateBmi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class WeightViewModel(
    private val repository: WeightRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    init {
        loadRecords()
        observeRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = repository.getRecordCount()
            _uiState.update { it.copy(recordCount = count, isLoading = false) }
        }
    }

    private fun observeRecords() {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun addOrUpdateWeight(weight: Double, date: LocalDate, note: String = "") {
        viewModelScope.launch {
            val existingRecord = repository.getRecordByDate(date)
            if (existingRecord != null) {
                repository.updateRecord(existingRecord.copy(weight = weight, note = note))
            } else {
                repository.insertRecord(WeightRecord(weight = weight, date = date, note = note))
            }
            loadRecords()
        }
    }

    fun deleteRecord(record: WeightRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            loadRecords()
        }
    }

    fun updateRecord(record: WeightRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
            loadRecords()
        }
    }

    fun getChartData(startDate: LocalDate, endDate: LocalDate, height: Float): Flow<List<WeightWithBmi>> {
        return repository.getRecordsBetween(startDate, endDate).map { records ->
            val filledData = mutableListOf<WeightWithBmi>()
            val sortedRecords = records.sortedBy { it.date }
            val today = LocalDate.now()
            
            val actualEndDate = if (endDate.isAfter(today)) today else endDate
            val actualStartDate = startDate
            
            var lastKnownWeight: Double? = null
            
            var currentDate = actualStartDate
            while (!currentDate.isAfter(actualEndDate)) {
                val record = records.find { it.date == currentDate }
                if (record != null) {
                    lastKnownWeight = record.weight
                    filledData.add(WeightWithBmi(record.weight, calculateBmi(record.weight, height), currentDate))
                } else if (lastKnownWeight != null) {
                    filledData.add(WeightWithBmi(lastKnownWeight, calculateBmi(lastKnownWeight, height), currentDate))
                } else {
                    val futureRecord = sortedRecords.find { !it.date.isBefore(currentDate) }
                    if (futureRecord != null) {
                        lastKnownWeight = futureRecord.weight
                        filledData.add(WeightWithBmi(futureRecord.weight, calculateBmi(futureRecord.weight, height), currentDate))
                    }
                }
                currentDate = currentDate.plusDays(1)
            }
            
            if (sortedRecords.isNotEmpty() && filledData.isNotEmpty()) {
                val lastData = filledData.last()
                val latestRecord = sortedRecords.last()
                if (lastData.date.isBefore(today)) {
                    filledData.add(WeightWithBmi(latestRecord.weight, calculateBmi(latestRecord.weight, height), today))
                }
            }
            
            filledData
        }
    }

    fun getStatistics(records: List<WeightRecord>, height: Float, targetWeight: Float): Statistics {
        if (records.isEmpty()) {
            return Statistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        val sortedRecords = records.sortedBy { it.date }
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val lastWeek = today.minusDays(7)
        val thirtyDaysAgo = today.minusDays(30)

        val yesterdayRecord = sortedRecords.find { it.date == yesterday }
        val lastWeekRecord = sortedRecords.find { it.date == lastWeek }
        val latestRecord = sortedRecords.lastOrNull()

        val thirtyDayRecords = sortedRecords.filter { !it.date.isBefore(thirtyDaysAgo) }

        val changeFromYesterday = if (yesterdayRecord != null && latestRecord != null) {
            latestRecord.weight - yesterdayRecord.weight
        } else 0.0

        val changeFromLastWeek = if (lastWeekRecord != null && latestRecord != null) {
            latestRecord.weight - lastWeekRecord.weight
        } else 0.0

        val averageWeight = if (thirtyDayRecords.isNotEmpty()) {
            thirtyDayRecords.map { it.weight }.average()
        } else 0.0

        val maxWeight = thirtyDayRecords.maxOfOrNull { it.weight } ?: 0.0
        val minWeight = thirtyDayRecords.minOfOrNull { it.weight } ?: 0.0

        val targetDiff = if (latestRecord != null) {
            latestRecord.weight - targetWeight
        } else 0.0

        return Statistics(
            changeFromYesterday = changeFromYesterday,
            changeFromLastWeek = changeFromLastWeek,
            averageWeight = averageWeight,
            maxWeight = maxWeight,
            minWeight = minWeight,
            targetDiff = targetDiff
        )
    }

    fun exportToCsv(records: List<WeightRecord>): String {
        val sb = StringBuilder()
        sb.appendLine("日期,体重(kg),备注")
        records.sortedBy { it.date }.forEach { record ->
            sb.appendLine("${record.date},${record.weight},${record.note}")
        }
        return sb.toString()
    }

    fun importFromCsv(csvContent: String) {
        viewModelScope.launch {
            val lines = csvContent.lines()
            if (lines.isEmpty()) return@launch

            for (i in 1 until lines.size) {
                val line = lines[i]
                val parts = line.split(",")
                if (parts.size >= 2) {
                    try {
                        val date = LocalDate.parse(parts[0].trim())
                        val weight = parts[1].trim().toDouble()
                        val note = if (parts.size > 2) parts[2].trim() else ""
                        repository.insertRecord(WeightRecord(weight = weight, date = date, note = note))
                    } catch (e: Exception) {
                        // Skip invalid lines
                    }
                }
            }
            loadRecords()
        }
    }
}

data class WeightUiState(
    val records: List<WeightRecord> = emptyList(),
    val isLoading: Boolean = false,
    val recordCount: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTimeRange: Int = 7
)

class WeightViewModelFactory(
    private val repository: WeightRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightViewModel(repository, settingsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
