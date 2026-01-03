package com.example.foodtracking.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodtracking.data.model.DailyLimits
import com.example.foodtracking.data.preferences.SettingsManager
import com.example.foodtracking.ui.theme.*
import kotlin.math.roundToInt

enum class ActivityLevel(val label: String, val multiplier: Float) {
    SEDENTARY("Sedentary (little/no exercise)", 1.2f),
    LIGHT("Light (1-3 days/week)", 1.375f),
    MODERATE("Moderate (3-5 days/week)", 1.55f),
    ACTIVE("Active (6-7 days/week)", 1.725f),
    VERY_ACTIVE("Very Active (athlete)", 1.9f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    settingsManager: SettingsManager,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    // User data
    var age by remember { mutableStateOf("") }
    var currentWeight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var weeksToGoal by remember { mutableStateOf("8") }
    var isMale by remember { mutableStateOf(true) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var showActivityDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // App logo/title
            Text(
                text = "🥗",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Food Tracker",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Let's set up your personal goals",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1) / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 32.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "step"
            ) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            0 -> {
                                // Basic info
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "About You",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { age = it.filter { c -> c.isDigit() } },
                                    label = { Text("Age") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Text("🎂") }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Gender selector
                                Text(
                                    text = "Gender",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = isMale,
                                        onClick = { isMale = true },
                                        label = { Text("♂ Male") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = !isMale,
                                        onClick = { isMale = false },
                                        label = { Text("♀ Female") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Activity level
                                ExposedDropdownMenuBox(
                                    expanded = showActivityDropdown,
                                    onExpandedChange = { showActivityDropdown = it }
                                ) {
                                    OutlinedTextField(
                                        value = activityLevel.label,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Activity Level") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showActivityDropdown) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = showActivityDropdown,
                                        onDismissRequest = { showActivityDropdown = false }
                                    ) {
                                        ActivityLevel.entries.forEach { level ->
                                            DropdownMenuItem(
                                                text = { Text(level.label) },
                                                onClick = {
                                                    activityLevel = level
                                                    showActivityDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            1 -> {
                                // Weight goals
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Your Goal",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                OutlinedTextField(
                                    value = currentWeight,
                                    onValueChange = { currentWeight = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Current Weight (kg)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = { Text("⚖️") }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = targetWeight,
                                    onValueChange = { targetWeight = it.filter { c -> c.isDigit() || c == '.' } },
                                    label = { Text("Target Weight (kg)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = { Text("🎯") }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = weeksToGoal,
                                    onValueChange = { weeksToGoal = it.filter { c -> c.isDigit() } },
                                    label = { Text("Weeks to reach goal") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Text("📅") }
                                )
                            }
                            
                            2 -> {
                                // Summary
                                val currW = currentWeight.toFloatOrNull() ?: 75f
                                val targW = targetWeight.toFloatOrNull() ?: 70f
                                val weeks = weeksToGoal.toIntOrNull() ?: 8
                                val userAge = age.toIntOrNull() ?: 30
                                
                                val (calories, deficit) = calculateCalories(
                                    age = userAge,
                                    weight = currW,
                                    targetWeight = targW,
                                    weeks = weeks,
                                    isMale = isMale,
                                    activityLevel = activityLevel
                                )
                                
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Green40
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Your Plan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    text = "$calories",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CaloriesColor
                                )
                                Text(
                                    text = "calories per day",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (deficit > 0) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Green40.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Text(
                                            text = "📉 ${deficit.roundToInt()} kcal daily deficit for weight loss",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else if (deficit < 0) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = ProteinsColor.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Text(
                                            text = "📈 ${(-deficit).roundToInt()} kcal daily surplus for weight gain",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Macros
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    MacroPreview("Protein", "${(calories * 0.25 / 4).roundToInt()}g", ProteinsColor)
                                    MacroPreview("Carbs", "${(calories * 0.45 / 4).roundToInt()}g", CarbsColor)
                                    MacroPreview("Fat", "${(calories * 0.30 / 9).roundToInt()}g", FatsColor)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back")
                    }
                }
                
                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep++
                        } else {
                            // Save and complete
                            val currW = currentWeight.toFloatOrNull() ?: 75f
                            val targW = targetWeight.toFloatOrNull() ?: 70f
                            val weeks = weeksToGoal.toIntOrNull() ?: 8
                            val userAge = age.toIntOrNull() ?: 30
                            
                            settingsManager.saveUserProfile(
                                age = userAge,
                                currentWeight = currW,
                                targetWeight = targW,
                                weeksToGoal = weeks,
                                isMale = isMale,
                                activityLevel = activityLevel.ordinal
                            )
                            
                            val (calories, _) = calculateCalories(userAge, currW, targW, weeks, isMale, activityLevel)
                            val proteins = (calories * 0.25 / 4).roundToInt()
                            val carbs = (calories * 0.45 / 4).roundToInt()
                            val fats = (calories * 0.30 / 9).roundToInt()
                            
                            settingsManager.saveDailyLimits(DailyLimits(
                                calories = calories,
                                fats = fats.toFloat(),
                                proteins = proteins.toFloat(),
                                carbs = carbs.toFloat()
                            ))
                            
                            settingsManager.setOnboardingComplete()
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (currentStep) {
                        0 -> age.isNotBlank()
                        1 -> currentWeight.isNotBlank() && targetWeight.isNotBlank() && weeksToGoal.isNotBlank()
                        else -> true
                    }
                ) {
                    Text(if (currentStep < 2) "Next" else "Get Started")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(if (currentStep < 2) Icons.Default.ArrowForward else Icons.Default.Check, null)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MacroPreview(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Calculate daily calories based on weight goal.
 * Returns (targetCalories, dailyDeficit)
 */
private fun calculateCalories(
    age: Int,
    weight: Float,
    targetWeight: Float,
    weeks: Int,
    isMale: Boolean,
    activityLevel: ActivityLevel
): Pair<Int, Float> {
    // Estimate BMR using simplified Mifflin-St Jeor (without height, using average)
    // Assuming average height: 175cm for men, 162cm for women
    val estimatedHeight = if (isMale) 175f else 162f
    val bmr = if (isMale) {
        10 * weight + 6.25f * estimatedHeight - 5 * age + 5
    } else {
        10 * weight + 6.25f * estimatedHeight - 5 * age - 161
    }
    
    // TDEE (maintenance calories)
    val tdee = bmr * activityLevel.multiplier
    
    // Calculate deficit/surplus needed
    val weightDiff = weight - targetWeight // positive = losing, negative = gaining
    val totalCalorieChange = weightDiff * 7700 // 1kg fat ≈ 7700 kcal
    val dailyDeficit = if (weeks > 0) totalCalorieChange / (weeks * 7) else 0f
    
    // Target calories (with safety limits)
    val targetCalories = (tdee - dailyDeficit).coerceIn(1200f, 4000f)
    
    return Pair(targetCalories.roundToInt(), dailyDeficit)
}
