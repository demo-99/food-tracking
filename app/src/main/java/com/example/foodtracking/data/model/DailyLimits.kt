package com.example.foodtracking.data.model

/**
 * User's daily nutrition limits.
 */
data class DailyLimits(
    val calories: Int = 2000,
    val fats: Float = 65f,
    val proteins: Float = 50f,
    val carbs: Float = 300f
)

/**
 * Aggregated nutrition data for a day.
 */
data class DailySummary(
    val totalCalories: Int = 0,
    val totalFats: Float = 0f,
    val totalProteins: Float = 0f,
    val totalCarbs: Float = 0f
) {
    fun progressCalories(limits: DailyLimits): Float = 
        (totalCalories.toFloat() / limits.calories).coerceIn(0f, 1f)
    
    fun progressFats(limits: DailyLimits): Float = 
        (totalFats / limits.fats).coerceIn(0f, 1f)
    
    fun progressProteins(limits: DailyLimits): Float = 
        (totalProteins / limits.proteins).coerceIn(0f, 1f)
    
    fun progressCarbs(limits: DailyLimits): Float = 
        (totalCarbs / limits.carbs).coerceIn(0f, 1f)
}
