package com.example.foodtracking.ui.addfood

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.foodtracking.ai.FoodAnalysisResult
import com.example.foodtracking.ui.theme.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
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
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth(),
                indicator = { tabPositions ->
                    if (uiState.selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier
                                .wrapContentSize(Alignment.BottomStart)
                                .offset(x = tabPositions[uiState.selectedTab].left)
                                .width(tabPositions[uiState.selectedTab].width)
                        )
                    }
                }
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("📷 Photo") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("📝 Describe") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("♥️ Favorites") }
                )
                Tab(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    text = { Text("🔍 Search") }
                )
                Tab(
                    selected = uiState.selectedTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    text = { Text("✏️ Manual") }
                )
            }

            // Error message
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, "Dismiss")
                        }
                    }
                }
            }

            when (uiState.selectedTab) {
                0 -> PhotoTab(viewModel, uiState, context)
                1 -> DescriptionTab(viewModel, uiState)
                2 -> FavoritesTab(viewModel, uiState, snackbarHostState)
                3 -> SearchTab(viewModel, uiState)
                4 -> ManualTab(viewModel, uiState)
            }
        }
    }
}

@Composable
private fun PhotoTab(
    viewModel: AddFoodViewModel,
    uiState: AddFoodUiState,
    context: Context
) {
    var photoFile by remember { mutableStateOf<File?>(null) }
    
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoFile?.let { file ->
                val bitmap = loadAndRotateBitmap(file.absolutePath)
                viewModel.setCapturedPhoto(bitmap)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            bitmap?.let { bmp -> viewModel.setCapturedPhoto(bmp) }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "images").apply { mkdirs() }
                .let { File(it, "photo_${System.currentTimeMillis()}.jpg") }
            photoFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (uiState.capturedPhoto != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        bitmap = uiState.capturedPhoto.asImageBitmap(),
                        contentDescription = "Captured food",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Take Photo", style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gallery", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }

        if (uiState.capturedPhoto != null && uiState.analysisResult == null) {
            // Hint text field for additional context
            item {
                OutlinedTextField(
                    value = uiState.photoHint,
                    onValueChange = { viewModel.setPhotoHint(it) },
                    label = { Text("Additional info (optional)") },
                    placeholder = { Text("e.g., \"chicken with rice, about 300g\" or \"homemade, low oil\"") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2,
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setCapturedPhoto(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change")
                    }
                    Button(
                        onClick = { viewModel.analyzePhoto() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze")
                        }
                    }
                }
            }
        }

        uiState.getEffectiveResult()?.let { result ->
            val originalWeight = uiState.analysisResult?.weightGrams ?: result.weightGrams
            item {
                AnalysisResultCard(
                    result = result,
                    originalWeight = originalWeight,
                    onWeightChanged = { viewModel.updateWeight(it) },
                    onSave = { viewModel.saveFromAnalysis() }
                )
            }
        }
    }
}

@Composable
private fun DescriptionTab(
    viewModel: AddFoodViewModel,
    uiState: AddFoodUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Describe what you ate") },
                placeholder = { Text("e.g., 2 eggs, toast with butter, orange juice") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            Button(
                onClick = { viewModel.analyzeDescription() },
                enabled = uiState.description.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Analyze with AI")
            }
        }

        uiState.getEffectiveResult()?.let { result ->
            val originalWeight = uiState.analysisResult?.weightGrams ?: result.weightGrams
            item {
                AnalysisResultCard(
                    result = result,
                    originalWeight = originalWeight,
                    onWeightChanged = { viewModel.updateWeight(it) },
                    onSave = { viewModel.saveFromAnalysis() }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun FavoritesTab(
    viewModel: AddFoodViewModel,
    uiState: AddFoodUiState,
    snackbarHostState: SnackbarHostState
) {
    if (uiState.favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "💔", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "No favorites yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Swipe right on foods in the main screen to favorite them",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val haptics = LocalHapticFeedback.current
        val scope = rememberCoroutineScope()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = uiState.favorites, key = { it.id }) { favorite ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.removeFavorite(favorite)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${favorite.name} removed from favorites",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreFavorite(favorite)
                                }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )

                LaunchedEffect(dismissState.targetValue) {
                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                
                SwipeToDismissBox(
                    modifier = Modifier.animateItem(),
                    state = dismissState,
                    backgroundContent = {
                        val color = MaterialTheme.colorScheme.errorContainer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(12.dp))
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    content = {
                        FavoriteCard(
                            favorite = favorite,
                            onClick = { viewModel.saveFromFavorites(favorite) }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteCard(
    favorite: com.example.foodtracking.data.model.FavoriteFood,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(text = favorite.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = favorite.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${favorite.calories} kcal", style = MaterialTheme.typography.titleSmall, color = CaloriesColor, fontWeight = FontWeight.Bold)
                Text(text = "P:${favorite.proteins.toInt()}g C:${favorite.carbs.toInt()}g F:${favorite.fats.toInt()}g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SearchTab(
    viewModel: AddFoodViewModel,
    uiState: AddFoodUiState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.searchFood(it) },
            label = { Text("Search food") },
            placeholder = { Text("e.g., banana, chicken, pizza...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.searchResults) { food ->
                SearchResultCard(food = food, onClick = { viewModel.saveFromSearch(food) })
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    food: com.example.foodtracking.data.model.CommonFood,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(text = food.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(text = food.servingSize, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${food.calories} kcal", style = MaterialTheme.typography.titleSmall, color = CaloriesColor, fontWeight = FontWeight.Bold)
                Text(text = "P:${food.proteins.toInt()}g C:${food.carbs.toInt()}g F:${food.fats.toInt()}g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ManualTab(
    viewModel: AddFoodViewModel,
    uiState: AddFoodUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.manualName,
                onValueChange = { viewModel.updateManualField(ManualField.NAME, it) },
                label = { Text("Food name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        item {
            OutlinedTextField(
                value = uiState.manualWeight,
                onValueChange = { viewModel.updateManualField(ManualField.WEIGHT, it) },
                label = { Text("Weight (grams)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            OutlinedTextField(
                value = uiState.manualCalories,
                onValueChange = { viewModel.updateManualField(ManualField.CALORIES, it) },
                label = { Text("Calories (kcal)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.manualProteins,
                    onValueChange = { viewModel.updateManualField(ManualField.PROTEINS, it) },
                    label = { Text("Protein (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = uiState.manualCarbs,
                    onValueChange = { viewModel.updateManualField(ManualField.CARBS, it) },
                    label = { Text("Carbs (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = uiState.manualFats,
                    onValueChange = { viewModel.updateManualField(ManualField.FATS, it) },
                    label = { Text("Fat (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveManual() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.manualName.isNotBlank()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Food")
            }
        }
    }
}

@Composable
private fun AnalysisResultCard(
    result: FoodAnalysisResult,
    originalWeight: Int,
    onWeightChanged: (Int) -> Unit,
    onSave: () -> Unit
) {
    var weightText by remember(result.weightGrams) { mutableStateOf(result.weightGrams.toString()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = result.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "✨ AI Analysis", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = result.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weight control section
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Portion Size", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    
                    // Manual weight input
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { newValue ->
                            weightText = newValue.filter { it.isDigit() }
                            weightText.toIntOrNull()?.let { weight ->
                                if (weight > 0) onWeightChanged(weight)
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
                val percentage = if (originalWeight > 0) (result.weightGrams * 100 / originalWeight) else 100
                Text(
                    text = "${percentage}% of estimated portion (${originalWeight}g)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Slider: 0% to 300% of original weight, with 100% at 1/3 position
                // Range: 0 to originalWeight * 3
                val maxWeight = originalWeight * 3
                Slider(
                    value = result.weightGrams.toFloat(),
                    onValueChange = { newWeight ->
                        val weight = newWeight.roundToInt().coerceAtLeast(1)
                        weightText = weight.toString()
                        onWeightChanged(weight)
                    },
                    valueRange = 0f..maxWeight.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Labels for slider positions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f), modifier = Modifier.offset(x = (-40).dp))
                    Text("200%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f), modifier = Modifier.offset(x = (-20).dp))
                    Text("300%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientDisplay("Calories", "${result.calories}", "kcal", CaloriesColor)
                NutrientDisplay("Protein", "${result.proteins.toInt()}", "g", ProteinsColor)
                NutrientDisplay("Carbs", "${result.carbs.toInt()}", "g", CarbsColor)
                NutrientDisplay("Fat", "${result.fats.toInt()}", "g", FatsColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Check, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Today")
            }
        }
    }
}

@Composable
private fun NutrientDisplay(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(text = unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun loadAndRotateBitmap(path: String): Bitmap {
    val bitmap = BitmapFactory.decodeFile(path)
    val exif = ExifInterface(path)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    return if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            context.contentResolver.openInputStream(uri)?.use { exifStream ->
                try {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    if (rotation != 0f && bitmap != null) {
                        val matrix = Matrix().apply { postRotate(rotation) }
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    } else {
                        bitmap
                    }
                } catch (e: Exception) {
                    bitmap
                }
            } ?: bitmap
        }
    } catch (e: Exception) {
        null
    }
}
