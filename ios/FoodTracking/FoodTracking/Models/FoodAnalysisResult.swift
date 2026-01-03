// FoodAnalysisResult - Result from AI analysis

import Foundation

struct FoodAnalysisResult {
    var name: String
    var calories: Int
    var fats: Float
    var proteins: Float
    var carbs: Float
    var emoji: String
    var weightGrams: Int
    var confidence: Float
    
    init(
        name: String = "Unknown Food",
        calories: Int = 0,
        fats: Float = 0,
        proteins: Float = 0,
        carbs: Float = 0,
        emoji: String = "🍽️",
        weightGrams: Int = 100,
        confidence: Float = 0.8
    ) {
        self.name = name
        self.calories = calories
        self.fats = fats
        self.proteins = proteins
        self.carbs = carbs
        self.emoji = emoji
        self.weightGrams = weightGrams
        self.confidence = confidence
    }
    
    /// Scale the result to a new weight
    func scaledTo(newWeightGrams: Int) -> FoodAnalysisResult {
        guard weightGrams > 0, newWeightGrams != weightGrams else { return self }
        let ratio = Float(newWeightGrams) / Float(weightGrams)
        return FoodAnalysisResult(
            name: name,
            calories: Int(Float(calories) * ratio),
            fats: fats * ratio,
            proteins: proteins * ratio,
            carbs: carbs * ratio,
            emoji: emoji,
            weightGrams: newWeightGrams,
            confidence: confidence
        )
    }
    
    func toFoodEntry(source: FoodSource = .photo) -> FoodEntry {
        FoodEntry(
            name: name,
            calories: calories,
            fats: fats,
            proteins: proteins,
            carbs: carbs,
            emoji: emoji,
            weightGrams: weightGrams,
            source: source
        )
    }
}
