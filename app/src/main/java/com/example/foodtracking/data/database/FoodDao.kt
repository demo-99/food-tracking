package com.example.foodtracking.data.database

import androidx.room.*
import com.example.foodtracking.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for FoodEntry.
 */
@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries ORDER BY date DESC, timestamp DESC")
    fun getAllEntries(): Flow<List<FoodEntry>>

    @Query("SELECT DISTINCT date FROM food_entries ORDER BY date DESC")
    fun getAllDates(): Flow<List<LocalDate>>
    
    @Query("SELECT * FROM food_entries WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp DESC")
    suspend fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<FoodEntry>

    @Query("""
        SELECT 
            COALESCE(SUM(calories), 0) as totalCalories,
            COALESCE(SUM(fats), 0.0) as totalFats,
            COALESCE(SUM(proteins), 0.0) as totalProteins,
            COALESCE(SUM(carbs), 0.0) as totalCarbs
        FROM food_entries 
        WHERE date = :date
    """)
    fun getDailySummary(date: LocalDate): Flow<DailySummaryTuple>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntry): Long

    @Update
    suspend fun update(entry: FoodEntry)

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/**
 * Tuple for daily summary query.
 */
data class DailySummaryTuple(
    val totalCalories: Int,
    val totalFats: Float,
    val totalProteins: Float,
    val totalCarbs: Float
)
