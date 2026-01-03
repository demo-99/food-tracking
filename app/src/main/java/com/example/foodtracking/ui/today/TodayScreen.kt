package com.example.foodtracking.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodtracking.data.model.FoodEntry
import com.example.foodtracking.ui.components.CircularProgressIndicator
import com.example.foodtracking.ui.components.FoodEditDialog
import com.example.foodtracking.ui.components.FoodEntryCard
import com.example.foodtracking.ui.theme.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onAddFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()
    val limits by viewModel.dailyLimits.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val favoriteNames by viewModel.favoriteNames.collectAsState()
    
    // Edit dialog state
    var editingEntry by remember { mutableStateOf<FoodEntry?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.inversePrimary
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddFood,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Food") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date header
            item {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Progress overview card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CircularProgressIndicator(
                                progress = summary.progressCalories(limits),
                                color = CaloriesColor,
                                label = "Calories",
                                value = "${summary.totalCalories}",
                                maxValue = "${limits.calories}"
                            )
                            CircularProgressIndicator(
                                progress = summary.progressProteins(limits),
                                color = ProteinsColor,
                                label = "Protein",
                                value = "${summary.totalProteins.toInt()}g",
                                maxValue = "${limits.proteins.toInt()}g"
                            )
                            CircularProgressIndicator(
                                progress = summary.progressCarbs(limits),
                                color = CarbsColor,
                                label = "Carbs",
                                value = "${summary.totalCarbs.toInt()}g",
                                maxValue = "${limits.carbs.toInt()}g"
                            )
                            CircularProgressIndicator(
                                progress = summary.progressFats(limits),
                                color = FatsColor,
                                label = "Fat",
                                value = "${summary.totalFats.toInt()}g",
                                maxValue = "${limits.fats.toInt()}g"
                            )
                        }
                    }
                }
            }
            
            // Swipe hint
            item {
                Text(
                    text = "← Swipe left to delete • Swipe right to favorite →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Food entries section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Food",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${entries.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🍽️",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No food logged yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the button below to add your first meal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    FoodEntryCard(
                        modifier = Modifier.animateItem(),
                        entry = entry,
                        isFavorite = favoriteNames.contains(entry.name),
                        onEdit = { editingEntry = entry },
                        onDelete = { 
                            viewModel.deleteEntry(entry)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Deleted ${entry.name}",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreEntry(entry)
                                }
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(entry) }
                    )
                }
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
    
    // Edit dialog
    editingEntry?.let { entry ->
        FoodEditDialog(
            entry = entry,
            isFavorite = favoriteNames.contains(entry.name),
            onDismiss = { editingEntry = null },
            onSave = { updatedEntry -> 
                viewModel.updateEntry(updatedEntry)
                editingEntry = null
            },
            onToggleFavorite = { viewModel.toggleFavorite(entry) },
            onDelete = { 
                viewModel.deleteEntry(entry)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted ${entry.name}",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreEntry(entry)
                    }
                }
                editingEntry = null
            }
        )
    }
}
