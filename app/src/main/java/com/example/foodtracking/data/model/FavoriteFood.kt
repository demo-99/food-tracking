package com.example.foodtracking.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a favorite food item saved by the user.
 */
@Entity(tableName = "favorite_foods")
data class FavoriteFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val fats: Float,
    val proteins: Float,
    val carbs: Float,
    val emoji: String = "🍽️",
    val weightGrams: Int = 100 // Default weight
) {
    fun toFoodEntry(date: java.time.LocalDate, timestamp: java.time.LocalDateTime = java.time.LocalDateTime.now()): FoodEntry {
        return FoodEntry(
            name = name,
            calories = calories,
            fats = fats,
            proteins = proteins,
            carbs = carbs,
            emoji = emoji,
            weightGrams = weightGrams,
            date = date,
            timestamp = timestamp,
            source = FoodSource.DATABASE
        )
    }
    
    companion object {
        fun fromFoodEntry(entry: FoodEntry): FavoriteFood {
            return FavoriteFood(
                name = entry.name,
                calories = entry.calories,
                fats = entry.fats,
                proteins = entry.proteins,
                carbs = entry.carbs,
                emoji = entry.emoji,
                weightGrams = entry.weightGrams
            )
        }
    }
}
