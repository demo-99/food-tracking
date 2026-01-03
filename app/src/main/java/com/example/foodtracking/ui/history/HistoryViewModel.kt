package com.example.foodtracking.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodtracking.data.model.DailySummary
import com.example.foodtracking.data.model.FoodEntry
import com.example.foodtracking.data.repository.FoodRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class DayHistory(
    val date: LocalDate,
    val entries: List<FoodEntry>,
    val summary: DailySummary
)

class HistoryViewModel(
    private val repository: FoodRepository
) : ViewModel() {

    val historyByDate: StateFlow<List<DayHistory>> = repository.getAllEntries()
        .map { entries ->
            entries.groupBy { it.date }
                .map { (date, dayEntries) ->
                    DayHistory(
                        date = date,
                        entries = dayEntries,
                        summary = DailySummary(
                            totalCalories = dayEntries.sumOf { it.calories },
                            totalFats = dayEntries.map { it.fats }.sum(),
                            totalProteins = dayEntries.map { it.proteins }.sum(),
                            totalCarbs = dayEntries.map { it.carbs }.sum()
                        )
                    )
                }
                .sortedByDescending { it.date }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val repository: FoodRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
