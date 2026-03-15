package com.weighttracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("MM/dd")
    private val displayFullFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatDisplayDate(date: LocalDate): String = date.format(displayFormatter)

    fun formatFullDate(date: LocalDate): String = date.format(displayFullFormatter)

    fun parseDate(dateString: String): LocalDate = LocalDate.parse(dateString, dateFormatter)
}
