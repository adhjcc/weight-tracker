package com.weighttracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double,
    val date: LocalDate,
    val note: String = ""
)
