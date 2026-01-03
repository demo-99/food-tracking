package com.example.foodtracking.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.foodtracking.ui.onboarding.ActivityLevel
import com.example.foodtracking.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showActivityDropdown by remember { mutableStateOf(false) }
    var showCalorieDetails by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Daily Limits Section with expandable details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = CaloriesColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily Limits",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Auto-saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Main calorie display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${uiState.calories} kcal",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CaloriesColor
                                )
                                if (uiState.dailyDeficit > 0) {
                                    Text(
                                        text = if (uiState.isLosingWeight) 
                                            "📉 ${uiState.dailyDeficit} kcal deficit" 
                                        else 
                                            "📈 ${uiState.dailyDeficit} kcal surplus",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Macro summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroDisplay("Protein", "${uiState.proteins}g", ProteinsColor)
                            MacroDisplay("Carbs", "${uiState.carbs}g", CarbsColor)
                            MacroDisplay("Fat", "${uiState.fats}g", FatsColor)
                        }

                        // Expandable details
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCalorieDetails = !showCalorieDetails },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚙️ Adjust calculation",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    if (showCalorieDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showCalorieDetails,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider()
                                
                                // TDEE info
                                if (uiState.tdee > 0) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Maintenance calories (TDEE)")
                                            Text(
                                                "${uiState.tdee} kcal",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Profile fields
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.age,
                                        onValueChange = { viewModel.updateAge(it) },
                                        label = { Text("Age") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    
                                    // Gender chips
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Gender",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = uiState.isMale,
                                                onClick = { viewModel.setGender(true) },
                                                label = { Text("♂") }
                                            )
                                            FilterChip(
                                                selected = !uiState.isMale,
                                                onClick = { viewModel.setGender(false) },
                                                label = { Text("♀") }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.currentWeight,
                                        onValueChange = { viewModel.updateCurrentWeight(it) },
                                        label = { Text("Current (kg)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = uiState.targetWeight,
                                        onValueChange = { viewModel.updateTargetWeight(it) },
                                        label = { Text("Target (kg)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = uiState.weeksToGoal,
                                        onValueChange = { viewModel.updateWeeksToGoal(it) },
                                        label = { Text("Weeks") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                // Activity level
                                ExposedDropdownMenuBox(
                                    expanded = showActivityDropdown,
                                    onExpandedChange = { showActivityDropdown = it }
                                ) {
                                    OutlinedTextField(
                                        value = uiState.activityLevel.label,
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
                                                    viewModel.setActivityLevel(level)
                                                    showActivityDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider()

                                // Manual override
                                Text(
                                    text = "Or set manually:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = uiState.calories,
                                    onValueChange = { viewModel.updateCalories(it) },
                                    label = { Text("Calories (kcal)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CaloriesColor
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.proteins,
                                        onValueChange = { viewModel.updateProteins(it) },
                                        label = { Text("Protein (g)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ProteinsColor
                                        )
                                    )
                                    OutlinedTextField(
                                        value = uiState.carbs,
                                        onValueChange = { viewModel.updateCarbs(it) },
                                        label = { Text("Carbs (g)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CarbsColor
                                        )
                                    )
                                    OutlinedTextField(
                                        value = uiState.fats,
                                        onValueChange = { viewModel.updateFats(it) },
                                        label = { Text("Fat (g)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FatsColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // API Key Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI API Key",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Required for photo and description analysis. Get your key from ai.google.dev",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = { viewModel.updateApiKey(it) },
                            label = { Text("API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (uiState.showApiKey) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleApiKeyVisibility() }) {
                                    Icon(
                                        if (uiState.showApiKey) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            }
                        )

                        AnimatedContent(
                            targetState = uiState.apiKeySaved,
                            transitionSpec = {
                                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                            },
                            label = "saveButton"
                        ) { saved ->
                            if (saved) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(Icons.Default.Check, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Saved Successfully!")
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.saveApiKey() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Save, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save API Key")
                                }
                            }
                        }
                    }
                }
            }

            // Health Connect Section
            item {
                HealthConnectSection(
                    uiState = uiState,
                    viewModel = viewModel,
                    context = context,
                    coroutineScope = coroutineScope
                )
            }

            // About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Food Tracking App v1.0",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Track your daily nutrition with AI-powered food analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MacroDisplay(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val TAG = "HealthConnectUI"

@Composable
private fun HealthConnectSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    context: Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    // Log the current state
    android.util.Log.d(TAG, "HealthConnectSection - available: ${uiState.healthConnectAvailable}, needsUpdate: ${uiState.healthConnectNeedsUpdate}, permissionGranted: ${uiState.healthConnectPermissionGranted}")
    
    // Create the permission launcher unconditionally at the top level
    val permissionContract = viewModel.createPermissionContract()
    android.util.Log.d(TAG, "Permission contract created: ${permissionContract != null}")
    
    val permissionLauncher = if (permissionContract != null) {
        rememberLauncherForActivityResult(contract = permissionContract) { grantedPermissions ->
            android.util.Log.d(TAG, "Permission result callback - granted: $grantedPermissions")
            viewModel.refreshHealthConnectPermissions()
        }
    } else null
    
    android.util.Log.d(TAG, "Permission launcher created: ${permissionLauncher != null}")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Connect",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Sync your nutrition data with Google Fit, Samsung Health, and other health apps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                !uiState.healthConnectAvailable && uiState.healthConnectNeedsUpdate -> {
                    // Health Connect needs update
                    Button(
                        onClick = {
                            viewModel.getHealthConnectIntent()?.let {
                                context.startActivity(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Update, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Health Connect")
                    }
                }
                !uiState.healthConnectAvailable -> {
                    // Health Connect not installed / not available
                    Text(
                        text = "Health Connect is not available on this device. Requires Android 14+ or Health Connect app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.getHealthConnectIntent()?.let {
                                try {
                                    context.startActivity(it)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Could not open Play Store",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Installing Health Connect")
                    }
                }
                !uiState.healthConnectPermissionGranted -> {
                    // Need permissions
                    if (permissionLauncher != null) {
                        Button(
                            onClick = {
                                android.util.Log.d(TAG, "Grant Permissions button clicked")
                                val permissions = viewModel.getHealthConnectPermissions()
                                android.util.Log.d(TAG, "Requesting permissions: $permissions")
                                try {
                                    permissionLauncher.launch(permissions)
                                    android.util.Log.d(TAG, "Permission launcher launched successfully")
                                } catch (e: Exception) {
                                    android.util.Log.e(TAG, "Error launching permissions", e)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Error launching permissions: ${e.message}",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Security, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Permissions")
                        }
                    } else {
                        Text(
                            text = "Health Connect permissions unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    // Connected and has permissions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.syncToHealthConnect()
                            }
                        },
                        enabled = !uiState.healthConnectSyncing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.healthConnectSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Sync, null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Last 7 Days")
                    }
                    
                    uiState.healthConnectSyncResult?.let { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (result.startsWith("✓")) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
