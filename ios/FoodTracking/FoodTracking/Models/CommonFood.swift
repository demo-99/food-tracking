// CommonFood - Static database of common foods for search

import Foundation

struct CommonFood: Identifiable {
    let id = UUID()
    let name: String
    let emoji: String
    let calories: Int
    let proteins: Float
    let carbs: Float
    let fats: Float
    let servingSize: String
    let weightGrams: Int
    
    func toFoodEntry() -> FoodEntry {
        FoodEntry(
            name: name,
            calories: calories,
            fats: fats,
            proteins: proteins,
            carbs: carbs,
            emoji: emoji,
            weightGrams: weightGrams,
            source: .search
        )
    }
}

// Common foods database - matching Android's CommonFoodsDatabase
struct CommonFoodsDatabase {
    static let foods: [CommonFood] = [
        // Fruits
        CommonFood(name: "Apple", emoji: "🍎", calories: 95, proteins: 0.5, carbs: 25, fats: 0.3, servingSize: "1 medium", weightGrams: 182),
        CommonFood(name: "Banana", emoji: "🍌", calories: 105, proteins: 1.3, carbs: 27, fats: 0.4, servingSize: "1 medium", weightGrams: 118),
        CommonFood(name: "Orange", emoji: "🍊", calories: 62, proteins: 1.2, carbs: 15, fats: 0.2, servingSize: "1 medium", weightGrams: 131),
        CommonFood(name: "Strawberries", emoji: "🍓", calories: 49, proteins: 1, carbs: 12, fats: 0.5, servingSize: "1 cup", weightGrams: 152),
        CommonFood(name: "Grapes", emoji: "🍇", calories: 104, proteins: 1.1, carbs: 27, fats: 0.2, servingSize: "1 cup", weightGrams: 151),
        CommonFood(name: "Watermelon", emoji: "🍉", calories: 46, proteins: 0.9, carbs: 12, fats: 0.2, servingSize: "1 cup diced", weightGrams: 152),
        CommonFood(name: "Mango", emoji: "🥭", calories: 99, proteins: 1.4, carbs: 25, fats: 0.6, servingSize: "1 cup sliced", weightGrams: 165),
        CommonFood(name: "Pineapple", emoji: "🍍", calories: 82, proteins: 0.9, carbs: 22, fats: 0.2, servingSize: "1 cup chunks", weightGrams: 165),
        CommonFood(name: "Avocado", emoji: "🥑", calories: 234, proteins: 2.9, carbs: 12, fats: 21, servingSize: "1 medium", weightGrams: 150),
        
        // Proteins
        CommonFood(name: "Chicken Breast", emoji: "🍗", calories: 165, proteins: 31, carbs: 0, fats: 3.6, servingSize: "100g cooked", weightGrams: 100),
        CommonFood(name: "Salmon", emoji: "🐟", calories: 208, proteins: 20, carbs: 0, fats: 13, servingSize: "100g cooked", weightGrams: 100),
        CommonFood(name: "Egg", emoji: "🥚", calories: 78, proteins: 6, carbs: 0.6, fats: 5, servingSize: "1 large", weightGrams: 50),
        CommonFood(name: "Beef Steak", emoji: "🥩", calories: 271, proteins: 26, carbs: 0, fats: 18, servingSize: "100g cooked", weightGrams: 100),
        CommonFood(name: "Tuna", emoji: "🐟", calories: 132, proteins: 29, carbs: 0, fats: 1, servingSize: "100g canned", weightGrams: 100),
        CommonFood(name: "Shrimp", emoji: "🦐", calories: 99, proteins: 24, carbs: 0.2, fats: 0.3, servingSize: "100g cooked", weightGrams: 100),
        CommonFood(name: "Tofu", emoji: "🧊", calories: 76, proteins: 8, carbs: 1.9, fats: 4.8, servingSize: "100g", weightGrams: 100),
        
        // Dairy
        CommonFood(name: "Milk", emoji: "🥛", calories: 149, proteins: 8, carbs: 12, fats: 8, servingSize: "1 cup", weightGrams: 244),
        CommonFood(name: "Greek Yogurt", emoji: "🥛", calories: 100, proteins: 17, carbs: 6, fats: 0.7, servingSize: "170g container", weightGrams: 170),
        CommonFood(name: "Cheese (Cheddar)", emoji: "🧀", calories: 113, proteins: 7, carbs: 0.4, fats: 9, servingSize: "1 oz", weightGrams: 28),
        CommonFood(name: "Cottage Cheese", emoji: "🧀", calories: 163, proteins: 28, carbs: 6, fats: 2.3, servingSize: "1 cup", weightGrams: 226),
        
        // Grains & Carbs
        CommonFood(name: "White Rice", emoji: "🍚", calories: 206, proteins: 4.3, carbs: 45, fats: 0.4, servingSize: "1 cup cooked", weightGrams: 158),
        CommonFood(name: "Brown Rice", emoji: "🍚", calories: 216, proteins: 5, carbs: 45, fats: 1.8, servingSize: "1 cup cooked", weightGrams: 195),
        CommonFood(name: "Pasta", emoji: "🍝", calories: 220, proteins: 8, carbs: 43, fats: 1.3, servingSize: "1 cup cooked", weightGrams: 140),
        CommonFood(name: "Bread (White)", emoji: "🍞", calories: 79, proteins: 2.7, carbs: 15, fats: 1, servingSize: "1 slice", weightGrams: 30),
        CommonFood(name: "Bread (Whole Wheat)", emoji: "🍞", calories: 81, proteins: 4, carbs: 14, fats: 1.1, servingSize: "1 slice", weightGrams: 33),
        CommonFood(name: "Oatmeal", emoji: "🥣", calories: 158, proteins: 6, carbs: 27, fats: 3.2, servingSize: "1 cup cooked", weightGrams: 234),
        CommonFood(name: "Quinoa", emoji: "🍚", calories: 222, proteins: 8, carbs: 39, fats: 4, servingSize: "1 cup cooked", weightGrams: 185),
        
        // Vegetables
        CommonFood(name: "Broccoli", emoji: "🥦", calories: 55, proteins: 3.7, carbs: 11, fats: 0.6, servingSize: "1 cup chopped", weightGrams: 156),
        CommonFood(name: "Carrot", emoji: "🥕", calories: 25, proteins: 0.6, carbs: 6, fats: 0.1, servingSize: "1 medium", weightGrams: 61),
        CommonFood(name: "Spinach", emoji: "🥬", calories: 7, proteins: 0.9, carbs: 1.1, fats: 0.1, servingSize: "1 cup raw", weightGrams: 30),
        CommonFood(name: "Tomato", emoji: "🍅", calories: 22, proteins: 1.1, carbs: 4.8, fats: 0.2, servingSize: "1 medium", weightGrams: 123),
        CommonFood(name: "Potato", emoji: "🥔", calories: 161, proteins: 4.3, carbs: 37, fats: 0.2, servingSize: "1 medium", weightGrams: 173),
        CommonFood(name: "Sweet Potato", emoji: "🍠", calories: 103, proteins: 2.3, carbs: 24, fats: 0.1, servingSize: "1 medium", weightGrams: 114),
        CommonFood(name: "Corn", emoji: "🌽", calories: 96, proteins: 3.4, carbs: 21, fats: 1.5, servingSize: "1 medium ear", weightGrams: 90),
        
        // Fast Food & Prepared
        CommonFood(name: "Pizza (Cheese)", emoji: "🍕", calories: 285, proteins: 12, carbs: 36, fats: 10, servingSize: "1 slice", weightGrams: 107),
        CommonFood(name: "Hamburger", emoji: "🍔", calories: 354, proteins: 20, carbs: 29, fats: 17, servingSize: "1 burger", weightGrams: 156),
        CommonFood(name: "Hot Dog", emoji: "🌭", calories: 314, proteins: 11, carbs: 24, fats: 19, servingSize: "1 with bun", weightGrams: 116),
        CommonFood(name: "French Fries", emoji: "🍟", calories: 365, proteins: 4, carbs: 48, fats: 17, servingSize: "medium serving", weightGrams: 117),
        CommonFood(name: "Taco", emoji: "🌮", calories: 226, proteins: 9, carbs: 20, fats: 12, servingSize: "1 taco", weightGrams: 102),
        CommonFood(name: "Burrito", emoji: "🌯", calories: 431, proteins: 21, carbs: 49, fats: 17, servingSize: "1 burrito", weightGrams: 227),
        CommonFood(name: "Sushi Roll", emoji: "🍣", calories: 255, proteins: 9, carbs: 38, fats: 7, servingSize: "6 pieces", weightGrams: 158),
        CommonFood(name: "Ramen", emoji: "🍜", calories: 436, proteins: 10, carbs: 57, fats: 17, servingSize: "1 bowl", weightGrams: 425),
        
        // Snacks
        CommonFood(name: "Almonds", emoji: "🥜", calories: 164, proteins: 6, carbs: 6, fats: 14, servingSize: "1 oz (23 nuts)", weightGrams: 28),
        CommonFood(name: "Peanut Butter", emoji: "🥜", calories: 188, proteins: 8, carbs: 6, fats: 16, servingSize: "2 tbsp", weightGrams: 32),
        CommonFood(name: "Dark Chocolate", emoji: "🍫", calories: 170, proteins: 2, carbs: 13, fats: 12, servingSize: "1 oz", weightGrams: 28),
        CommonFood(name: "Popcorn", emoji: "🍿", calories: 93, proteins: 3, carbs: 19, fats: 1.1, servingSize: "3 cups popped", weightGrams: 24),
        CommonFood(name: "Chips (Potato)", emoji: "🍟", calories: 152, proteins: 2, carbs: 15, fats: 10, servingSize: "1 oz", weightGrams: 28),
        
        // Beverages
        CommonFood(name: "Orange Juice", emoji: "🍊", calories: 112, proteins: 1.7, carbs: 26, fats: 0.5, servingSize: "1 cup", weightGrams: 248),
        CommonFood(name: "Coffee (black)", emoji: "☕", calories: 2, proteins: 0.3, carbs: 0, fats: 0, servingSize: "1 cup", weightGrams: 237),
        CommonFood(name: "Coca-Cola", emoji: "🥤", calories: 140, proteins: 0, carbs: 39, fats: 0, servingSize: "12 oz can", weightGrams: 355),
        CommonFood(name: "Beer", emoji: "🍺", calories: 153, proteins: 1.6, carbs: 13, fats: 0, servingSize: "12 oz", weightGrams: 355),
        CommonFood(name: "Wine (Red)", emoji: "🍷", calories: 125, proteins: 0.1, carbs: 4, fats: 0, servingSize: "5 oz glass", weightGrams: 148),
        
        // Desserts
        CommonFood(name: "Ice Cream", emoji: "🍦", calories: 207, proteins: 3.5, carbs: 24, fats: 11, servingSize: "1/2 cup", weightGrams: 66),
        CommonFood(name: "Cake (Chocolate)", emoji: "🍰", calories: 352, proteins: 5, carbs: 51, fats: 14, servingSize: "1 slice", weightGrams: 95),
        CommonFood(name: "Cookie (Chocolate Chip)", emoji: "🍪", calories: 78, proteins: 0.9, carbs: 9, fats: 4.5, servingSize: "1 medium", weightGrams: 16),
        CommonFood(name: "Donut", emoji: "🍩", calories: 253, proteins: 4, carbs: 30, fats: 14, servingSize: "1 medium", weightGrams: 60),
    ]
    
    static func search(query: String) -> [CommonFood] {
        guard !query.isEmpty else { return [] }
        let lowercasedQuery = query.lowercased()
        return foods.filter { $0.name.lowercased().contains(lowercasedQuery) }
    }
}
