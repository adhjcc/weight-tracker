package com.weighttracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: LocalDate): WeightRecord?

    @Query("SELECT * FROM weight_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<WeightRecord>>

    @Query("SELECT * FROM weight_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatestRecord(): WeightRecord?

    @Query("SELECT * FROM weight_records WHERE date >= :date ORDER BY date ASC LIMIT 1")
    suspend fun getNextRecord(date: LocalDate): WeightRecord?

    @Query("SELECT * FROM weight_records WHERE date <= :date ORDER BY date DESC LIMIT 1")
    suspend fun getPreviousRecord(date: LocalDate): WeightRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WeightRecord): Long

    @Update
    suspend fun updateRecord(record: WeightRecord)

    @Delete
    suspend fun deleteRecord(record: WeightRecord)

    @Query("DELETE FROM weight_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM weight_records")
    suspend fun getRecordCount(): Int
}
