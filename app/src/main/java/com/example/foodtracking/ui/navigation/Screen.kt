package com.example.foodtracking.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Today : Screen("today", "Today", Icons.Default.Today)
    data object History : Screen("history", "History", Icons.Default.CalendarMonth)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object AddFood : Screen("add_food", "Add Food")
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.History,
    Screen.Settings
)
