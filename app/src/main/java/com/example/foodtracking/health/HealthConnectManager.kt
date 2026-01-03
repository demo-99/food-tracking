package com.example.foodtracking.health

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.units.Energy


import androidx.health.connect.client.units.Mass
import com.example.foodtracking.data.model.FoodEntry
import java.time.ZoneId

/**
 * Manager for Health Connect integration.
 * Handles permissions and data sync for nutrition data.
 */
class HealthConnectManager(private val context: Context) {

    /**
     * Delete a food entry from Health Connect.
     */
    suspend fun deleteFoodRecord(recordId: String): Result<Unit> {
        return try {
            healthConnectClient.deleteRecords(
                NutritionRecord::class,
                listOf(recordId),
                emptyList<String>()
            )
            Log.d(TAG, "Deleted food entry with ID: $recordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting food entry", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "HealthConnectManager"
        private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
        
        // Permissions needed for nutrition data
        val PERMISSIONS = setOf(
            HealthPermission.getWritePermission(NutritionRecord::class),
            HealthPermission.getReadPermission(NutritionRecord::class)
        )
    }
    
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    /**
     * Get the SDK status as a displayable string.
     */
    fun getSdkStatusString(): String {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> "Available"
            HealthConnectClient.SDK_UNAVAILABLE -> "Unavailable (Android 14+ or Health Connect app required)"
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Update required"
            else -> "Unknown status"
        }
    }
    
    /**
     * Check if Health Connect is available on this device.
     */
    fun isAvailable(): Boolean {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.d(TAG, "Health Connect SDK status: $status (${getSdkStatusString()})")
        return status == HealthConnectClient.SDK_AVAILABLE
    }
    
    /**
     * Check if Health Connect is installed but needs update.
     */
    fun needsUpdate(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
    }
    
    /**
     * Check if Play Store is available.
     */
    private fun isPlayStoreAvailable(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.android.vending", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Open Health Connect installation/update - returns true if intent was launched.
     */
    fun openHealthConnectInstall(): Boolean {
        return try {
            val intent = if (isPlayStoreAvailable()) {
                // Try Play Store first
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                // Fallback to web browser
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            
            context.startActivity(intent)
            Log.d(TAG, "Opened Health Connect install page")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Health Connect install", e)
            Toast.makeText(
                context, 
                "Could not open store. Health Connect requires Android 14+ or manual installation.", 
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
    
    /**
     * Get the intent to install/update Health Connect.
     */
    fun getHealthConnectIntent(): Intent {
        return if (isPlayStoreAvailable()) {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
    
    /**
     * Check if we have the required permissions.
     */
    suspend fun hasPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            val hasAll = PERMISSIONS.all { it in granted }
            Log.d(TAG, "Has permissions: $hasAll (granted: ${granted.size}, required: ${PERMISSIONS.size})")
            hasAll
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }
    
    /**
     * Create a permission request contract.
     */
    fun createPermissionContract() = PermissionController.createRequestPermissionResultContract()
    
    /**
     * Check if a record still exists in Health Connect.
     */
    suspend fun recordExists(recordId: String): Boolean {
        return try {
            // Query records from the last 30 days to check if our record exists
            val endTime = java.time.Instant.now()
            val startTime = endTime.minus(java.time.Duration.ofDays(30))
            
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = androidx.health.connect.client.time.TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.any { it.metadata.id == recordId }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if record exists: $recordId", e)
            // If we can't check, assume it doesn't exist to trigger re-sync
            false
        }
    }
    
    /**
     * Sync a food entry to Health Connect.
     * Returns the Health Connect record ID on success.
     */
    suspend fun syncFoodEntry(entry: FoodEntry): Result<String> {
        return try {
            val zoneId = ZoneId.systemDefault()
            val startTime = entry.timestamp.atZone(zoneId).toInstant()
            // Assume meal duration of 15 minutes
            val endTime = startTime.plusSeconds(15 * 60)
            
            val nutritionRecord = NutritionRecord(
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneId.rules.getOffset(startTime),
                endZoneOffset = zoneId.rules.getOffset(endTime),
                name = entry.name,
                // Use kilocalories - food calories are kcal, not small calories
                energy = Energy.kilocalories(entry.calories.toDouble()),
                protein = Mass.grams(entry.proteins.toDouble()),
                totalCarbohydrate = Mass.grams(entry.carbs.toDouble()),
                totalFat = Mass.grams(entry.fats.toDouble())
            )
            
            val insertResult = healthConnectClient.insertRecords(listOf(nutritionRecord))
            val recordId = insertResult.recordIdsList.firstOrNull() 
                ?: return Result.failure(Exception("No record ID returned"))
            
            Log.d(TAG, "Synced food entry: ${entry.name} with ID: $recordId")
            Result.success(recordId)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing food entry", e)
            Result.failure(e)
        }
    }

    /**
     * Update an existing food entry in Health Connect.
     */
    suspend fun updateFoodEntry(recordId: String, entry: FoodEntry): Result<Unit> {
        return try {
            val zoneId = ZoneId.systemDefault()
            val startTime = entry.timestamp.atZone(zoneId).toInstant()
            val endTime = startTime.plusSeconds(15 * 60)
            
            val nutritionRecord = NutritionRecord(
                metadata = androidx.health.connect.client.records.metadata.Metadata(id = recordId),
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneId.rules.getOffset(startTime),
                endZoneOffset = zoneId.rules.getOffset(endTime),
                name = entry.name,
                energy = Energy.kilocalories(entry.calories.toDouble()),
                protein = Mass.grams(entry.proteins.toDouble()),
                totalCarbohydrate = Mass.grams(entry.carbs.toDouble()),
                totalFat = Mass.grams(entry.fats.toDouble())
            )
            
            healthConnectClient.updateRecords(listOf(nutritionRecord))
            Log.d(TAG, "Updated food entry: ${entry.name} with ID: $recordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating food entry", e)
            Result.failure(e)
        }
    }


}
