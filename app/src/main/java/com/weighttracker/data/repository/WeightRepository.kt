package com.weighttracker.data.repository

import com.weighttracker.data.local.WeightRecord
import com.weighttracker.data.local.WeightRecordDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WeightRepository(private val weightRecordDao: WeightRecordDao) {
    fun getAllRecords(): Flow<List<WeightRecord>> = weightRecordDao.getAllRecords()

    fun getRecordsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<WeightRecord>> {
        return weightRecordDao.getRecordsBetween(startDate, endDate)
    }

    suspend fun getRecordByDate(date: LocalDate): WeightRecord? {
        return weightRecordDao.getRecordByDate(date)
    }

    suspend fun getLatestRecord(): WeightRecord? {
        return weightRecordDao.getLatestRecord()
    }

    suspend fun getNextRecord(date: LocalDate): WeightRecord? {
        return weightRecordDao.getNextRecord(date)
    }

    suspend fun getPreviousRecord(date: LocalDate): WeightRecord? {
        return weightRecordDao.getPreviousRecord(date)
    }

    suspend fun insertRecord(record: WeightRecord): Long {
        return weightRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: WeightRecord) {
        weightRecordDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: WeightRecord) {
        weightRecordDao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Long) {
        weightRecordDao.deleteRecordById(id)
    }

    suspend fun getRecordCount(): Int {
        return weightRecordDao.getRecordCount()
    }
}
