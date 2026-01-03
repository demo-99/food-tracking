package com.example.foodtracking.data.database

import androidx.room.*
import com.example.foodtracking.data.model.FavoriteFood
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for favorite foods.
 */
@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorite_foods ORDER BY name ASC")
    fun getAllFavorites(): Flow<List<FavoriteFood>>
    
    @Query("SELECT * FROM favorite_foods WHERE name = :name LIMIT 1")
    suspend fun getFavoriteByName(name: String): FavoriteFood?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE name = :name)")
    suspend fun isFavorite(name: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_foods WHERE name = :name)")
    fun isFavoriteFlow(name: String): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteFood)
    
    @Delete
    suspend fun delete(favorite: FavoriteFood)
    
    @Query("DELETE FROM favorite_foods WHERE name = :name")
    suspend fun deleteByName(name: String)
}
