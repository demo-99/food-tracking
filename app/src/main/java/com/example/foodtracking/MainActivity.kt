package com.example.foodtracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodtracking.data.database.FoodDatabase
import com.example.foodtracking.data.preferences.SettingsManager
import com.example.foodtracking.data.repository.FoodRepository
import com.example.foodtracking.ui.addfood.AddFoodScreen
import com.example.foodtracking.ui.addfood.AddFoodViewModel
import com.example.foodtracking.ui.history.HistoryScreen
import com.example.foodtracking.ui.history.HistoryViewModel
import com.example.foodtracking.ui.navigation.Screen
import com.example.foodtracking.ui.navigation.bottomNavItems
import com.example.foodtracking.ui.onboarding.OnboardingScreen
import com.example.foodtracking.ui.settings.SettingsScreen
import com.example.foodtracking.ui.settings.SettingsViewModel
import com.example.foodtracking.ui.theme.FoodTrackingTheme
import com.example.foodtracking.ui.today.TodayScreen
import com.example.foodtracking.ui.today.TodayViewModel
import com.example.foodtracking.health.HealthConnectManager

class MainActivity : ComponentActivity() {
    
    private val database by lazy { FoodDatabase.getDatabase(this) }
    private val repository by lazy { FoodRepository(database.foodDao(), database.favoriteDao()) }
    private val settingsManager by lazy { SettingsManager(this) }
    private val healthConnectManager by lazy { HealthConnectManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodTrackingTheme {
                var showOnboarding by remember { 
                    mutableStateOf(!settingsManager.isOnboardingComplete()) 
                }
                
                if (showOnboarding) {
                    OnboardingScreen(
                        settingsManager = settingsManager,
                        onComplete = { showOnboarding = false }
                    )
                } else {
                    FoodTrackingApp(
                        repository = repository,
                        settingsManager = settingsManager,
                        healthConnectManager = healthConnectManager
                    )
                }
            }
        }
    }
}

@Composable
fun FoodTrackingApp(
    repository: FoodRepository,
    settingsManager: SettingsManager,
    healthConnectManager: HealthConnectManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on AddFood screen
    val showBottomBar = currentDestination?.route != Screen.AddFood.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                screen.icon?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                val viewModel: TodayViewModel = viewModel(
                    factory = TodayViewModel.Factory(repository, settingsManager, healthConnectManager)
                )
                TodayScreen(
                    viewModel = viewModel,
                    onAddFood = { navController.navigate(Screen.AddFood.route) }
                )
            }

            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(repository)
                )
                HistoryScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(settingsManager, healthConnectManager, repository)
                )
                SettingsScreen(viewModel = viewModel)
            }

            composable(Screen.AddFood.route) {
                val viewModel: AddFoodViewModel = viewModel(
                    factory = AddFoodViewModel.Factory(repository, settingsManager, healthConnectManager)
                )
                AddFoodScreen(
                    viewModel = viewModel,
                    onNavigateBack = { 
                        viewModel.resetState()
                        navController.popBackStack() 
                    }
                )
            }
        }
    }
}