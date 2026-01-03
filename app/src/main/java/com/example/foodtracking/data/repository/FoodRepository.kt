package com.example.foodtracking.data.repository

import com.example.foodtracking.data.database.FavoriteDao
import com.example.foodtracking.data.database.FoodDao
import com.example.foodtracking.data.model.DailySummary
import com.example.foodtracking.data.model.FavoriteFood
import com.example.foodtracking.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Repository for food entries and favorites data access.
 */
class FoodRepository(
    private val foodDao: FoodDao,
    private val favoriteDao: FavoriteDao? = null
) {
    
    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntry>> {
        return foodDao.getEntriesForDate(date)
    }

    fun getAllEntries(): Flow<List<FoodEntry>> {
        return foodDao.getAllEntries()
    }

    fun getAllDates(): Flow<List<LocalDate>> {
        return foodDao.getAllDates()
    }
    
    suspend fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<FoodEntry> {
        return foodDao.getEntriesForDateRange(startDate, endDate)
    }

    fun getDailySummary(date: LocalDate): Flow<DailySummary> {
        return foodDao.getDailySummary(date).map { tuple ->
            DailySummary(
                totalCalories = tuple.totalCalories,
                totalFats = tuple.totalFats,
                totalProteins = tuple.totalProteins,
                totalCarbs = tuple.totalCarbs
            )
        }
    }

    suspend fun insert(entry: FoodEntry): Long {
        return foodDao.insert(entry)
    }

    suspend fun update(entry: FoodEntry) {
        foodDao.update(entry)
    }

    suspend fun delete(entry: FoodEntry) {
        foodDao.delete(entry)
    }

    suspend fun deleteById(id: Long) {
        foodDao.deleteById(id)
    }

    // Favorites methods
    fun getAllFavorites(): Flow<List<FavoriteFood>> {
        return favoriteDao?.getAllFavorites() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun isFavorite(name: String): Boolean {
        return favoriteDao?.isFavorite(name) ?: false
    }

    suspend fun addToFavorites(entry: FoodEntry) {
        favoriteDao?.insert(FavoriteFood.fromFoodEntry(entry))
    }

    suspend fun removeFromFavorites(name: String) {
        favoriteDao?.deleteByName(name)
    }

    suspend fun deleteFavorite(favorite: FavoriteFood) {
        favoriteDao?.delete(favorite)
    }

    suspend fun insertFavorite(favorite: FavoriteFood) {
        favoriteDao?.insert(favorite)
    }

    suspend fun toggleFavorite(entry: FoodEntry) {
        if (favoriteDao == null) return
        if (favoriteDao.isFavorite(entry.name)) {
            favoriteDao.deleteByName(entry.name)
        } else {
            favoriteDao.insert(FavoriteFood.fromFoodEntry(entry))
        }
    }
}
