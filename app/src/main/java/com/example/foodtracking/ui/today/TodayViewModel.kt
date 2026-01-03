package com.example.foodtracking.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodtracking.data.model.DailyLimits
import com.example.foodtracking.data.model.DailySummary
import com.example.foodtracking.data.model.FavoriteFood
import com.example.foodtracking.data.model.FoodEntry
import com.example.foodtracking.data.preferences.SettingsManager
import com.example.foodtracking.data.repository.FoodRepository
import com.example.foodtracking.health.HealthConnectManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(
    private val repository: FoodRepository,
    private val settingsManager: SettingsManager,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val dailyLimits: StateFlow<DailyLimits> = settingsManager.dailyLimits

    val entries: StateFlow<List<FoodEntry>> = _selectedDate.flatMapLatest { date ->
        repository.getEntriesForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummary: StateFlow<DailySummary> = _selectedDate.flatMapLatest { date ->
        repository.getDailySummary(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())

    // Favorites
    val favorites: StateFlow<List<FavoriteFood>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Set of favorite food names for quick lookup
    val favoriteNames: StateFlow<Set<String>> = favorites.map { list ->
        list.map { it.name }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun deleteEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            if (entry.healthConnectRecordId != null) {
                healthConnectManager.deleteFoodRecord(entry.healthConnectRecordId)
            }
        }
    }

    fun restoreEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.insert(entry.copy(id = 0, healthConnectRecordId = null))
        }
    }

    fun toggleFavorite(entry: FoodEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
        }
    }

    fun updateEntry(entry: FoodEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }

    class Factory(
        private val repository: FoodRepository,
        private val settingsManager: SettingsManager,
        private val healthConnectManager: HealthConnectManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(repository, settingsManager, healthConnectManager) as T
        }
    }
}
