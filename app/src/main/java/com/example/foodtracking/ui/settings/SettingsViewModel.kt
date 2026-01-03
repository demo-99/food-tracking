package com.example.foodtracking.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodtracking.data.model.DailyLimits
import com.example.foodtracking.data.preferences.SettingsManager
import com.example.foodtracking.ui.onboarding.ActivityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SettingsUiState(
    val calories: String = "2000",
    val fats: String = "65",
    val proteins: String = "50",
    val carbs: String = "300",
    val apiKey: String = "",
    val showApiKey: Boolean = false,
    val apiKeySaved: Boolean = false,
    // User profile
    val age: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",
    val weeksToGoal: String = "8",
    val isMale: Boolean = true,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    // Calculation details
    val tdee: Int = 0,
    val dailyDeficit: Int = 0,
    val isLosingWeight: Boolean = true,
    // Health Connect
    val healthConnectAvailable: Boolean = false,
    val healthConnectNeedsUpdate: Boolean = false,
    val healthConnectPermissionGranted: Boolean = false,
    val healthConnectSyncing: Boolean = false,
    val healthConnectSyncResult: String? = null
)

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val healthConnectManager: com.example.foodtracking.health.HealthConnectManager? = null,
    private val repository: com.example.foodtracking.data.repository.FoodRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkHealthConnectStatus()
    }
    
    private fun checkHealthConnectStatus() {
        viewModelScope.launch {
            val isAvailable = healthConnectManager?.isAvailable() ?: false
            val needsUpdate = healthConnectManager?.needsUpdate() ?: false
            val hasPermissions = if (isAvailable) healthConnectManager?.hasPermissions() ?: false else false
            
            _uiState.value = _uiState.value.copy(
                healthConnectAvailable = isAvailable,
                healthConnectNeedsUpdate = needsUpdate,
                healthConnectPermissionGranted = hasPermissions
            )
        }
    }
    
    fun refreshHealthConnectPermissions() {
        checkHealthConnectStatus()
    }
    
    fun getHealthConnectIntent() = healthConnectManager?.getHealthConnectIntent()
    
    fun getHealthConnectPermissions() = com.example.foodtracking.health.HealthConnectManager.PERMISSIONS
    
    fun createPermissionContract() = healthConnectManager?.createPermissionContract()
    
    suspend fun syncToHealthConnect(): Result<Int> {
        val manager = healthConnectManager ?: return Result.failure(Exception("Health Connect not available"))
        val repo = repository ?: return Result.failure(Exception("Repository not available"))
        
        _uiState.value = _uiState.value.copy(healthConnectSyncing = true, healthConnectSyncResult = null)
        
        return try {
            val today = java.time.LocalDate.now()
            val weekAgo = today.minusDays(7)
            
            // Get all entries from the last week
            val allEntries = repo.getEntriesForDateRange(weekAgo, today)
            android.util.Log.d("HealthSync", "Found ${allEntries.size} entries from last week")
            
            // Check which entries need syncing or updating
            val entriesToInsert = mutableListOf<com.example.foodtracking.data.model.FoodEntry>()
            val entriesToUpdate = mutableListOf<com.example.foodtracking.data.model.FoodEntry>()
            
            for (entry in allEntries) {
                val recordId = entry.healthConnectRecordId
                android.util.Log.d("HealthSync", "Entry ${entry.name}: recordId=$recordId")
                
                if (recordId == null) {
                    // Never synced -> Insert
                    android.util.Log.d("HealthSync", "  -> No record ID, will insert")
                    entriesToInsert.add(entry)
                } else {
                    // Check if record still exists in Health Connect
                    val exists = manager.recordExists(recordId)
                    android.util.Log.d("HealthSync", "  -> Record exists check: $exists")
                    
                    if (exists) {
                        // Record exists -> Update it to ensure data correctness
                        android.util.Log.d("HealthSync", "  -> Record exists, will update")
                        entriesToUpdate.add(entry)
                    } else {
                        // Record was deleted from Health Connect -> Insert new
                        android.util.Log.d("HealthSync", "  -> Record deleted, will re-insert")
                        // Clear the old ID effectively by treating as new insert (logic below handles null recordId update)
                        entriesToInsert.add(entry)
                    }
                }
            }
            
            android.util.Log.d("HealthSync", "To Insert: ${entriesToInsert.size}, To Update: ${entriesToUpdate.size}")
            
            if (entriesToInsert.isEmpty() && entriesToUpdate.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    healthConnectSyncing = false,
                    healthConnectSyncResult = "✓ All ${allEntries.size} entries checked"
                )
                return Result.success(0)
            }
            
            var successCount = 0
            
            // 1. Handle Inserts
            for (entry in entriesToInsert) {
                android.util.Log.d("HealthSync", "Inserting: ${entry.name} (${entry.calories} kcal)")
                val result = manager.syncFoodEntry(entry)
                result.onSuccess { recordId ->
                    android.util.Log.d("HealthSync", "  -> Success! New ID: $recordId")
                    repo.update(entry.copy(healthConnectRecordId = recordId))
                    successCount++
                }.onFailure { e ->
                    android.util.Log.e("HealthSync", "  -> Insert Failed: ${e.message}")
                }
            }
            
            // 2. Handle Updates
            for (entry in entriesToUpdate) {
                android.util.Log.d("HealthSync", "Updating: ${entry.name} (${entry.calories} kcal)")
                val recordId = entry.healthConnectRecordId!!
                val result = manager.updateFoodEntry(recordId, entry)
                result.onSuccess {
                    android.util.Log.d("HealthSync", "  -> Update Success!")
                    successCount++
                }.onFailure { e ->
                    android.util.Log.e("HealthSync", "  -> Update Failed: ${e.message}")
                }
            }
            
            _uiState.value = _uiState.value.copy(
                healthConnectSyncing = false,
                healthConnectSyncResult = "✓ Synced/Updated $successCount entries"
            )
            
            Result.success(successCount)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                healthConnectSyncing = false,
                healthConnectSyncResult = "✗ Sync failed: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    fun clearSyncResult() {
        _uiState.value = _uiState.value.copy(healthConnectSyncResult = null)
    }

    private fun loadState(): SettingsUiState {
        val limits = settingsManager.dailyLimits.value
        val apiKey = settingsManager.geminiApiKey.value
        
        val age = settingsManager.getAge()
        val currentWeight = settingsManager.getCurrentWeight()
        val targetWeight = settingsManager.getTargetWeight()
        val weeks = settingsManager.getWeeksToGoal()
        val isMale = settingsManager.getIsMale()
        val activityLevel = ActivityLevel.entries.getOrElse(settingsManager.getActivityLevel()) { ActivityLevel.MODERATE }
        
        val (tdee, deficit) = calculateTdeeAndDeficit(age, currentWeight, targetWeight, weeks, isMale, activityLevel)
        
        return SettingsUiState(
            calories = limits.calories.toString(),
            fats = limits.fats.toInt().toString(),
            proteins = limits.proteins.toInt().toString(),
            carbs = limits.carbs.toInt().toString(),
            apiKey = apiKey,
            age = age.toString(),
            currentWeight = currentWeight.toString(),
            targetWeight = targetWeight.toString(),
            weeksToGoal = weeks.toString(),
            isMale = isMale,
            activityLevel = activityLevel,
            tdee = tdee,
            dailyDeficit = kotlin.math.abs(deficit.roundToInt()),
            isLosingWeight = currentWeight > targetWeight
        )
    }

    private fun calculateTdeeAndDeficit(
        age: Int,
        weight: Float,
        targetWeight: Float,
        weeks: Int,
        isMale: Boolean,
        activityLevel: ActivityLevel
    ): Pair<Int, Float> {
        val estimatedHeight = if (isMale) 175f else 162f
        val bmr = if (isMale) {
            10 * weight + 6.25f * estimatedHeight - 5 * age + 5
        } else {
            10 * weight + 6.25f * estimatedHeight - 5 * age - 161
        }
        val tdee = bmr * activityLevel.multiplier
        val weightDiff = weight - targetWeight
        val totalCalorieChange = weightDiff * 7700
        val dailyDeficit = if (weeks > 0) totalCalorieChange / (weeks * 7) else 0f
        return Pair(tdee.roundToInt(), dailyDeficit)
    }

    private fun autoSaveLimits() {
        val state = _uiState.value
        val limits = DailyLimits(
            calories = state.calories.toIntOrNull() ?: 2000,
            fats = state.fats.toFloatOrNull() ?: 65f,
            proteins = state.proteins.toFloatOrNull() ?: 50f,
            carbs = state.carbs.toFloatOrNull() ?: 300f
        )
        settingsManager.saveDailyLimits(limits)
    }

    fun updateCalories(value: String) {
        _uiState.value = _uiState.value.copy(calories = value.filter { it.isDigit() })
        autoSaveLimits()
    }

    fun updateFats(value: String) {
        _uiState.value = _uiState.value.copy(fats = value.filter { it.isDigit() })
        autoSaveLimits()
    }

    fun updateProteins(value: String) {
        _uiState.value = _uiState.value.copy(proteins = value.filter { it.isDigit() })
        autoSaveLimits()
    }

    fun updateCarbs(value: String) {
        _uiState.value = _uiState.value.copy(carbs = value.filter { it.isDigit() })
        autoSaveLimits()
    }

    fun updateApiKey(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, apiKeySaved = false)
    }

    fun toggleApiKeyVisibility() {
        _uiState.value = _uiState.value.copy(showApiKey = !_uiState.value.showApiKey)
    }

    fun saveApiKey() {
        settingsManager.saveGeminiApiKey(_uiState.value.apiKey)
        _uiState.value = _uiState.value.copy(apiKeySaved = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(apiKeySaved = false)
        }
    }

    // Profile updates with auto-recalculation
    fun updateAge(value: String) {
        _uiState.value = _uiState.value.copy(age = value.filter { it.isDigit() })
        recalculateAndSave()
    }

    fun updateCurrentWeight(value: String) {
        _uiState.value = _uiState.value.copy(currentWeight = value.filter { it.isDigit() || it == '.' })
        recalculateAndSave()
    }

    fun updateTargetWeight(value: String) {
        _uiState.value = _uiState.value.copy(targetWeight = value.filter { it.isDigit() || it == '.' })
        recalculateAndSave()
    }

    fun updateWeeksToGoal(value: String) {
        _uiState.value = _uiState.value.copy(weeksToGoal = value.filter { it.isDigit() })
        recalculateAndSave()
    }

    fun setGender(isMale: Boolean) {
        _uiState.value = _uiState.value.copy(isMale = isMale)
        recalculateAndSave()
    }

    fun setActivityLevel(level: ActivityLevel) {
        _uiState.value = _uiState.value.copy(activityLevel = level)
        recalculateAndSave()
    }

    private fun recalculateAndSave() {
        val state = _uiState.value
        val age = state.age.toIntOrNull() ?: return
        val currentWeight = state.currentWeight.toFloatOrNull() ?: return
        val targetWeight = state.targetWeight.toFloatOrNull() ?: return
        val weeks = state.weeksToGoal.toIntOrNull()?.takeIf { it > 0 } ?: return

        // Save profile
        settingsManager.saveUserProfile(
            age = age,
            currentWeight = currentWeight,
            targetWeight = targetWeight,
            weeksToGoal = weeks,
            isMale = state.isMale,
            activityLevel = state.activityLevel.ordinal
        )

        // Calculate new values
        val (tdee, dailyDeficit) = calculateTdeeAndDeficit(
            age, currentWeight, targetWeight, weeks, state.isMale, state.activityLevel
        )
        val targetCalories = (tdee - dailyDeficit).coerceIn(1200f, 4000f).roundToInt()
        val proteins = (targetCalories * 0.25 / 4).roundToInt()
        val carbs = (targetCalories * 0.45 / 4).roundToInt()
        val fats = (targetCalories * 0.30 / 9).roundToInt()

        _uiState.value = state.copy(
            calories = targetCalories.toString(),
            proteins = proteins.toString(),
            carbs = carbs.toString(),
            fats = fats.toString(),
            tdee = tdee,
            dailyDeficit = kotlin.math.abs(dailyDeficit.roundToInt()),
            isLosingWeight = currentWeight > targetWeight
        )
        autoSaveLimits()
    }

    class Factory(
        private val settingsManager: SettingsManager,
        private val healthConnectManager: com.example.foodtracking.health.HealthConnectManager? = null,
        private val repository: com.example.foodtracking.data.repository.FoodRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsManager, healthConnectManager, repository) as T
        }
    }
}
