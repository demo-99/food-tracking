package com.example.foodtracking.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.foodtracking.data.model.FoodEntry
import com.example.foodtracking.ui.theme.*
import kotlin.math.roundToInt

/**
 * Dialog for editing a food entry.
 * Allows changing portion size, toggling favorite, and deleting.
 */
@Composable
fun FoodEditDialog(
    entry: FoodEntry,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onSave: (FoodEntry) -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val originalWeight = entry.weightGrams
    var currentWeight by remember { mutableIntStateOf(entry.weightGrams) }
    var weightText by remember { mutableStateOf(entry.weightGrams.toString()) }
    
    // Calculate scaled values based on original values
    val ratio = if (originalWeight > 0) currentWeight.toFloat() / originalWeight.toFloat() else 1f
    val scaledCalories = (entry.calories * ratio).toInt()
    val scaledProteins = entry.proteins * ratio
    val scaledCarbs = entry.carbs * ratio
    val scaledFats = entry.fats * ratio
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with emoji and name
                Text(
                    text = entry.emoji,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Weight control section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Portion Size",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Manual weight input
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { newValue ->
                                weightText = newValue.filter { it.isDigit() }
                                weightText.toIntOrNull()?.let { weight ->
                                    if (weight > 0) currentWeight = weight
                                }
                            },
                            suffix = { Text("g") },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Percentage indicator
                    val percentage = if (originalWeight > 0) (currentWeight * 100 / originalWeight) else 100
                    Text(
                        text = "${percentage}% of original portion (${originalWeight}g)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Slider: 0% to 300% of original weight
                    val maxWeight = originalWeight * 3
                    Slider(
                        value = currentWeight.toFloat(),
                        onValueChange = { newWeight ->
                            currentWeight = newWeight.roundToInt().coerceAtLeast(1)
                            weightText = currentWeight.toString()
                        },
                        valueRange = 0f..maxWeight.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Labels for slider positions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text("200%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text("300%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                    

                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Nutrition display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutrientColumn("Calories", "$scaledCalories", "kcal", CaloriesColor)
                    NutrientColumn("Protein", "${scaledProteins.toInt()}", "g", ProteinsColor)
                    NutrientColumn("Carbs", "${scaledCarbs.toInt()}", "g", CarbsColor)
                    NutrientColumn("Fat", "${scaledFats.toInt()}", "g", FatsColor)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Favorite toggle
                    IconButton(
                        onClick = onToggleFavorite
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save/Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val updatedEntry = entry.copy(
                                weightGrams = currentWeight,
                                calories = scaledCalories,
                                proteins = scaledProteins,
                                carbs = scaledCarbs,
                                fats = scaledFats
                            )
                            onSave(updatedEntry)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientColumn(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
