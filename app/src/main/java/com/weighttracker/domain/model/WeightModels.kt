package com.weighttracker.domain.model

data class WeightWithBmi(
    val weight: Double,
    val bmi: Double,
    val date: java.time.LocalDate
)

data class Statistics(
    val changeFromYesterday: Double,
    val changeFromLastWeek: Double,
    val averageWeight: Double,
    val maxWeight: Double,
    val minWeight: Double,
    val targetDiff: Double
)

enum class BmiCategory(val displayName: String) {
    UNDERWEIGHT("偏瘦"),
    NORMAL("正常"),
    OVERWEIGHT("偏胖"),
    OBESE("肥胖")
}

fun calculateBmi(weight: Double, height: Float): Double {
    val heightInMeters = height / 100
    return weight / (heightInMeters * heightInMeters)
}

fun getBmiCategory(bmi: Double): BmiCategory {
    return when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 24 -> BmiCategory.NORMAL
        bmi < 28 -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }
}
