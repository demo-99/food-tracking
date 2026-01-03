package com.example.foodtracking.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result of AI food analysis.
 */
data class FoodAnalysisResult(
    val name: String,
    val calories: Int,
    val fats: Float,
    val proteins: Float,
    val carbs: Float,
    val emoji: String = "🍽️",
    val weightGrams: Int = 100,
    val confidence: Float = 0.8f
) {
    /**
     * Scale the result to a new weight.
     */
    fun scaledTo(newWeightGrams: Int): FoodAnalysisResult {
        if (weightGrams == 0 || newWeightGrams == weightGrams) return this
        val ratio = newWeightGrams.toFloat() / weightGrams.toFloat()
        return copy(
            weightGrams = newWeightGrams,
            calories = (calories * ratio).toInt(),
            proteins = proteins * ratio,
            carbs = carbs * ratio,
            fats = fats * ratio
        )
    }
}

/**
 * Service for analyzing food using Gemini AI.
 */
class GeminiService(private val apiKey: String) {
    
    companion object {
        private const val TAG = "GeminiService"
    }
    
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 32
                topP = 1f
                maxOutputTokens = 2048
            }
        )
    }

    /**
     * Analyze a food photo and estimate nutritional values.
     * @param hint Optional additional context from user to improve accuracy
     */
    suspend fun analyzePhoto(bitmap: Bitmap, hint: String? = null): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting photo analysis with hint: $hint")
            
            val hintSection = if (!hint.isNullOrBlank()) {
                "\nAdditional context from user: \"$hint\"\nUse this information to improve your estimate.\n"
            } else ""
            
            val prompt = """
                Analyze this food image and provide nutritional estimates.
                $hintSection
                Respond ONLY in this exact format with numbers (no ranges, pick the middle estimate):
                NAME: [food name]
                EMOJI: [single emoji that best represents this food]
                WEIGHT: [estimated portion weight in grams]
                CALORIES: [total calories for this portion]
                FATS: [grams of fat]
                PROTEINS: [grams of protein]
                CARBS: [grams of carbohydrates]
                
                Estimate the visible portion size. Be concise with the food name.
                Choose an emoji that visually represents the food (e.g., 🍕 for pizza, 🍌 for banana).
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )

            Log.d(TAG, "Response text: ${response.text}")
            
            val text = response.text ?: return@withContext Result.failure(
                Exception("Empty response from AI")
            )
            
            parseResponse(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing photo", e)
            Result.failure(e)
        }
    }

    /**
     * Analyze a food description and estimate nutritional values.
     */
    suspend fun analyzeDescription(description: String): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting description analysis for: $description")
            val prompt = """
                Estimate the nutritional values for this food: "$description"
                
                Respond ONLY in this exact format with numbers (no ranges, pick the middle estimate):
                NAME: [concise food name]
                EMOJI: [single emoji that best represents this food]
                WEIGHT: [typical portion weight in grams]
                CALORIES: [total calories for this portion]
                FATS: [grams of fat]
                PROTEINS: [grams of protein]
                CARBS: [grams of carbohydrates]
                
                Consider typical portion sizes. Be concise with the food name.
                Choose an emoji that visually represents the food.
            """.trimIndent()

            val response = model.generateContent(prompt)
            
            Log.d(TAG, "Response text: ${response.text}")
            
            val text = response.text ?: return@withContext Result.failure(
                Exception("Empty response from AI")
            )
            
            parseResponse(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing description", e)
            Result.failure(e)
        }
    }

    private fun parseResponse(text: String): Result<FoodAnalysisResult> {
        return try {
            val lines = text.lines()
            var name = "Unknown Food"
            var emoji = "🍽️"
            var calories = 0
            var fats = 0f
            var proteins = 0f
            var carbs = 0f
            var weight = 100

            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("NAME:", ignoreCase = true) -> {
                        name = trimmed.substringAfter(":").trim()
                    }
                    trimmed.startsWith("EMOJI:", ignoreCase = true) -> {
                        val emojiText = trimmed.substringAfter(":").trim()
                        emoji = extractEmoji(emojiText)
                    }
                    trimmed.startsWith("WEIGHT:", ignoreCase = true) -> {
                        weight = extractNumber(trimmed.substringAfter(":")).toInt().coerceAtLeast(1)
                    }
                    trimmed.startsWith("CALORIES:", ignoreCase = true) -> {
                        calories = extractNumber(trimmed.substringAfter(":")).toInt()
                    }
                    trimmed.startsWith("FATS:", ignoreCase = true) -> {
                        fats = extractNumber(trimmed.substringAfter(":"))
                    }
                    trimmed.startsWith("PROTEINS:", ignoreCase = true) -> {
                        proteins = extractNumber(trimmed.substringAfter(":"))
                    }
                    trimmed.startsWith("CARBS:", ignoreCase = true) -> {
                        carbs = extractNumber(trimmed.substringAfter(":"))
                    }
                }
            }

            Log.d(TAG, "Parsed: name=$name, weight=${weight}g, calories=$calories")
            
            Result.success(
                FoodAnalysisResult(
                    name = name,
                    calories = calories,
                    fats = fats,
                    proteins = proteins,
                    carbs = carbs,
                    emoji = emoji,
                    weightGrams = weight
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response", e)
            Result.failure(Exception("Failed to parse AI response: ${e.message}"))
        }
    }

    private fun extractNumber(text: String): Float {
        val cleaned = text.trim()
            .replace(Regex("[^0-9.]"), "")
            .takeIf { it.isNotEmpty() } ?: "0"
        return cleaned.toFloatOrNull() ?: 0f
    }
    
    private fun extractEmoji(text: String): String {
        val emojiRegex = Regex("[\\p{So}\\p{Sc}\\p{Sk}\\p{Sm}]|[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+")
        val match = emojiRegex.find(text)
        return match?.value ?: "🍽️"
    }
}
