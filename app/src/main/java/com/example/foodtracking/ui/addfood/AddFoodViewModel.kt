package com.example.foodtracking.ui.addfood

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodtracking.ai.FoodAnalysisResult
import com.example.foodtracking.ai.GeminiService
import com.example.foodtracking.data.model.CommonFood
import com.example.foodtracking.data.model.CommonFoodsDatabase
import com.example.foodtracking.data.model.FavoriteFood
import com.example.foodtracking.data.model.FoodEntry
import com.example.foodtracking.data.model.FoodSource
import com.example.foodtracking.data.preferences.SettingsManager
import com.example.foodtracking.data.repository.FoodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddFoodUiState(
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val adjustedWeight: Int? = null, // User-adjusted weight
    val searchQuery: String = "",
    val searchResults: List<CommonFood> = emptyList(),
    val favorites: List<FavoriteFood> = emptyList(),
    val manualName: String = "",
    val manualCalories: String = "",
    val manualFats: String = "",
    val manualProteins: String = "",
    val manualCarbs: String = "",
    val manualWeight: String = "100",
    val capturedPhoto: Bitmap? = null,
    val photoHint: String = "", // Additional context for photo analysis
    val description: String = "",
    val savedSuccessfully: Boolean = false
) {
    /**
     * Get the effective (possibly adjusted) analysis result.
     */
    fun getEffectiveResult(): FoodAnalysisResult? {
        val original = analysisResult ?: return null
        val newWeight = adjustedWeight ?: return original
        return original.scaledTo(newWeight)
    }
}

class AddFoodViewModel(
    private val repository: FoodRepository,
    private val settingsManager: SettingsManager,
    private val healthConnectManager: com.example.foodtracking.health.HealthConnectManager?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    val hasApiKey: Boolean
        get() = settingsManager.hasGeminiApiKey()

    init {
        viewModelScope.launch {
            repository.getAllFavorites().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }
    
    private suspend fun syncToHealthConnect(entry: FoodEntry): FoodEntry {
        if (healthConnectManager == null) return entry
        
        // Sync to Health Connect
        val result = healthConnectManager.syncFoodEntry(entry)
        
        return result.fold(
            onSuccess = { recordId ->
                // Return entry with record ID
                entry.copy(healthConnectRecordId = recordId)
            },
            onFailure = {
                // Return original entry if sync failed
                entry
            }
        )
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index, errorMessage = null) }
    }

    fun setCapturedPhoto(bitmap: Bitmap?) {
        _uiState.update { 
            it.copy(
                capturedPhoto = bitmap, 
                analysisResult = null,
                adjustedWeight = null
            ) 
        }
    }

    fun setDescription(text: String) {
        _uiState.update { 
            it.copy(
                description = text, 
                analysisResult = null,
                adjustedWeight = null
            ) 
        }
    }
    
    fun updateWeight(weight: Int) {
        _uiState.update { it.copy(adjustedWeight = weight) }
    }
    
    fun setPhotoHint(hint: String) {
        _uiState.update { it.copy(photoHint = hint) }
    }

    fun analyzePhoto() {
        val bitmap = _uiState.value.capturedPhoto ?: return
        val hint = _uiState.value.photoHint
        val apiKey = settingsManager.geminiApiKey.value
        if (apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please set your Gemini API key in Settings") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, adjustedWeight = null) }
            try {
                val service = GeminiService(apiKey)
                val result = service.analyzePhoto(bitmap, hint.ifBlank { null })
                result.fold(
                    onSuccess = { analysis ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                analysisResult = analysis,
                                adjustedWeight = analysis.weightGrams
                            ) 
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun analyzeDescription() {
        val description = _uiState.value.description
        if (description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a food description") }
            return
        }
        val apiKey = settingsManager.geminiApiKey.value
        if (apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please set your Gemini API key in Settings") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, adjustedWeight = null) }
            try {
                val service = GeminiService(apiKey)
                val result = service.analyzeDescription(description)
                result.fold(
                    onSuccess = { analysis ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                analysisResult = analysis,
                                adjustedWeight = analysis.weightGrams
                            ) 
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun searchFood(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                searchResults = CommonFoodsDatabase.search(query)
            )
        }
    }

    fun updateManualField(field: ManualField, value: String) {
        _uiState.update { state ->
            when (field) {
                ManualField.NAME -> state.copy(manualName = value)
                ManualField.CALORIES -> state.copy(manualCalories = value.filter { it.isDigit() })
                ManualField.FATS -> state.copy(manualFats = value.filter { it.isDigit() || it == '.' })
                ManualField.PROTEINS -> state.copy(manualProteins = value.filter { it.isDigit() || it == '.' })
                ManualField.CARBS -> state.copy(manualCarbs = value.filter { it.isDigit() || it == '.' })
                ManualField.WEIGHT -> state.copy(manualWeight = value.filter { it.isDigit() })
            }
        }
    }

    fun saveFromAnalysis(imageUri: String? = null) {
        val result = _uiState.value.getEffectiveResult() ?: return
        viewModelScope.launch {
            var entry = FoodEntry(
                name = result.name,
                calories = result.calories,
                fats = result.fats,
                proteins = result.proteins,
                carbs = result.carbs,
                emoji = result.emoji,
                weightGrams = result.weightGrams,
                date = LocalDate.now(),
                imageUri = imageUri,
                source = if (_uiState.value.capturedPhoto != null) FoodSource.PHOTO else FoodSource.DESCRIPTION
            )
            val id = repository.insert(entry)
            entry = entry.copy(id = id)
            
            // Sync to Health Connect and update if successful
            val syncedEntry = syncToHealthConnect(entry)
            if (syncedEntry.healthConnectRecordId != null) {
                repository.update(syncedEntry)
            }
            
            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }

    fun saveFromSearch(food: CommonFood) {
        viewModelScope.launch {
            var entry = food.toFoodEntry(LocalDate.now())
            val id = repository.insert(entry)
            entry = entry.copy(id = id)
            
            // Sync to Health Connect and update if successful
            val syncedEntry = syncToHealthConnect(entry)
            if (syncedEntry.healthConnectRecordId != null) {
                repository.update(syncedEntry)
            }
            
            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }
    
    fun saveFromFavorites(favorite: FavoriteFood) {
        viewModelScope.launch {
            var entry = favorite.toFoodEntry(LocalDate.now())
            val id = repository.insert(entry)
            entry = entry.copy(id = id)
            
            // Sync to Health Connect and update if successful
            val syncedEntry = syncToHealthConnect(entry)
            if (syncedEntry.healthConnectRecordId != null) {
                repository.update(syncedEntry)
            }
            
            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }
    
    fun removeFavorite(favorite: FavoriteFood) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
        }
    }
    
    fun restoreFavorite(favorite: FavoriteFood) {
        viewModelScope.launch {
            repository.insertFavorite(favorite)
        }
    }

    fun saveManual() {
        val state = _uiState.value
        if (state.manualName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a food name") }
            return
        }

        viewModelScope.launch {
            var entry = FoodEntry(
                name = state.manualName,
                calories = state.manualCalories.toIntOrNull() ?: 0,
                fats = state.manualFats.toFloatOrNull() ?: 0f,
                proteins = state.manualProteins.toFloatOrNull() ?: 0f,
                carbs = state.manualCarbs.toFloatOrNull() ?: 0f,
                weightGrams = state.manualWeight.toIntOrNull() ?: 100,
                date = LocalDate.now(),
                source = FoodSource.MANUAL
            )
            val id = repository.insert(entry)
            entry = entry.copy(id = id)
            
            // Sync to Health Connect and update if successful
            val syncedEntry = syncToHealthConnect(entry)
            if (syncedEntry.healthConnectRecordId != null) {
                repository.update(syncedEntry)
            }
            
            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetState() {
        _uiState.value = AddFoodUiState()
    }

    class Factory(
        private val repository: FoodRepository,
        private val settingsManager: SettingsManager,
        private val healthConnectManager: com.example.foodtracking.health.HealthConnectManager?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddFoodViewModel(repository, settingsManager, healthConnectManager) as T
        }
    }
}

enum class ManualField {
    NAME, CALORIES, FATS, PROTEINS, CARBS, WEIGHT
}
