package com.example.foodtracking.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a food entry logged by the user.
 */
@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val fats: Float,
    val proteins: Float,
    val carbs: Float,
    val emoji: String = "🍽️",
    val weightGrams: Int = 100, // Portion weight in grams
    val date: LocalDate,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val imageUri: String? = null,
    val source: FoodSource = FoodSource.MANUAL,
    val healthConnectRecordId: String? = null // Health Connect record ID for sync tracking
) {
    /**
     * Scale the nutrition values based on a new weight.
     */
    fun scaledTo(newWeightGrams: Int): FoodEntry {
        if (weightGrams == 0 || newWeightGrams == weightGrams) return this
        val ratio = newWeightGrams.toFloat() / weightGrams.toFloat()
        return copy(
            weightGrams = newWeightGrams,
            calories = (calories * ratio).toInt(),
            proteins = proteins * ratio,
            carbs = carbs * ratio,
            fats = fats * ratio
        )
    }
}

/**
 * Source of the food entry.
 */
enum class FoodSource {
    PHOTO,
    DESCRIPTION,
    DATABASE,
    MANUAL
}
