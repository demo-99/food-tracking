// FavoriteFood Model - SwiftData equivalent of Room entity

import Foundation
import SwiftData

@Model
final class FavoriteFood {
    var id: UUID
    var name: String
    var calories: Int
    var fats: Float
    var proteins: Float
    var carbs: Float
    var emoji: String
    var weightGrams: Int
    var createdAt: Date
    
    init(
        id: UUID = UUID(),
        name: String,
        calories: Int,
        fats: Float = 0,
        proteins: Float = 0,
        carbs: Float = 0,
        emoji: String = "🍽️",
        weightGrams: Int = 100
    ) {
        self.id = id
        self.name = name
        self.calories = calories
        self.fats = fats
        self.proteins = proteins
        self.carbs = carbs
        self.emoji = emoji
        self.weightGrams = weightGrams
        self.createdAt = Date()
    }
    
    /// Convert to FoodEntry for today
    func toFoodEntry() -> FoodEntry {
        FoodEntry(
            name: name,
            calories: calories,
            fats: fats,
            proteins: proteins,
            carbs: carbs,
            emoji: emoji,
            weightGrams: weightGrams,
            date: Date(),
            source: .favorites
        )
    }
}
