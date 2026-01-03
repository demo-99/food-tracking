// FoodEntry Model - SwiftData equivalent of Room entity

import Foundation
import SwiftData

enum FoodSource: String, Codable {
    case photo = "PHOTO"
    case description = "DESCRIPTION"
    case search = "SEARCH"
    case favorites = "FAVORITES"
    case manual = "MANUAL"
}

@Model
final class FoodEntry {
    var id: UUID
    var name: String
    var calories: Int
    var fats: Float
    var proteins: Float
    var carbs: Float
    var emoji: String
    var weightGrams: Int
    var date: Date
    var imageUri: String?
    var source: String  // FoodSource raw value
    var healthKitRecordId: String?
    var createdAt: Date
    
    init(
        id: UUID = UUID(),
        name: String,
        calories: Int,
        fats: Float = 0,
        proteins: Float = 0,
        carbs: Float = 0,
        emoji: String = "🍽️",
        weightGrams: Int = 100,
        date: Date = Date(),
        imageUri: String? = nil,
        source: FoodSource = .manual,
        healthKitRecordId: String? = nil
    ) {
        self.id = id
        self.name = name
        self.calories = calories
        self.fats = fats
        self.proteins = proteins
        self.carbs = carbs
        self.emoji = emoji
        self.weightGrams = weightGrams
        self.date = date
        self.imageUri = imageUri
        self.source = source.rawValue
        self.healthKitRecordId = healthKitRecordId
        self.createdAt = Date()
    }
    
    var foodSource: FoodSource {
        FoodSource(rawValue: source) ?? .manual
    }
}

// Extension for date grouping
extension FoodEntry {
    var dateOnly: Date {
        Calendar.current.startOfDay(for: date)
    }
}
