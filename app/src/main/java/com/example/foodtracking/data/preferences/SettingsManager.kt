package com.example.foodtracking.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.foodtracking.data.model.DailyLimits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages app settings and preferences.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _dailyLimits = MutableStateFlow(loadDailyLimits())
    val dailyLimits: StateFlow<DailyLimits> = _dailyLimits.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(loadGeminiApiKey())
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private fun loadDailyLimits(): DailyLimits {
        return DailyLimits(
            calories = prefs.getInt(KEY_CALORIES_LIMIT, 2000),
            fats = prefs.getFloat(KEY_FATS_LIMIT, 65f),
            proteins = prefs.getFloat(KEY_PROTEINS_LIMIT, 50f),
            carbs = prefs.getFloat(KEY_CARBS_LIMIT, 300f)
        )
    }

    private fun loadGeminiApiKey(): String {
        return prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun saveDailyLimits(limits: DailyLimits) {
        prefs.edit()
            .putInt(KEY_CALORIES_LIMIT, limits.calories)
            .putFloat(KEY_FATS_LIMIT, limits.fats)
            .putFloat(KEY_PROTEINS_LIMIT, limits.proteins)
            .putFloat(KEY_CARBS_LIMIT, limits.carbs)
            .apply()
        _dailyLimits.value = limits
    }

    fun saveGeminiApiKey(apiKey: String) {
        prefs.edit()
            .putString(KEY_GEMINI_API_KEY, apiKey)
            .apply()
        _geminiApiKey.value = apiKey
    }

    fun hasGeminiApiKey(): Boolean {
        return _geminiApiKey.value.isNotBlank()
    }

    // Onboarding
    fun isOnboardingComplete(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    // User profile data
    fun saveUserProfile(
        age: Int,
        currentWeight: Float,
        targetWeight: Float,
        weeksToGoal: Int,
        isMale: Boolean,
        activityLevel: Int
    ) {
        prefs.edit()
            .putInt(KEY_AGE, age)
            .putFloat(KEY_CURRENT_WEIGHT, currentWeight)
            .putFloat(KEY_TARGET_WEIGHT, targetWeight)
            .putInt(KEY_WEEKS_TO_GOAL, weeksToGoal)
            .putBoolean(KEY_IS_MALE, isMale)
            .putInt(KEY_ACTIVITY_LEVEL, activityLevel)
            .apply()
    }

    fun getAge(): Int = prefs.getInt(KEY_AGE, 30)
    fun getCurrentWeight(): Float = prefs.getFloat(KEY_CURRENT_WEIGHT, 75f)
    fun getTargetWeight(): Float = prefs.getFloat(KEY_TARGET_WEIGHT, 70f)
    fun getWeeksToGoal(): Int = prefs.getInt(KEY_WEEKS_TO_GOAL, 8)
    fun getIsMale(): Boolean = prefs.getBoolean(KEY_IS_MALE, true)
    fun getActivityLevel(): Int = prefs.getInt(KEY_ACTIVITY_LEVEL, 2) // MODERATE

    companion object {
        private const val PREFS_NAME = "food_tracking_prefs"
        private const val KEY_CALORIES_LIMIT = "calories_limit"
        private const val KEY_FATS_LIMIT = "fats_limit"
        private const val KEY_PROTEINS_LIMIT = "proteins_limit"
        private const val KEY_CARBS_LIMIT = "carbs_limit"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_AGE = "user_age"
        private const val KEY_CURRENT_WEIGHT = "current_weight"
        private const val KEY_TARGET_WEIGHT = "target_weight"
        private const val KEY_WEEKS_TO_GOAL = "weeks_to_goal"
        private const val KEY_IS_MALE = "is_male"
        private const val KEY_ACTIVITY_LEVEL = "activity_level"
    }
}
