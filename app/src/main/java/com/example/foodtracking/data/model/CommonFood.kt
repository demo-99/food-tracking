package com.example.foodtracking.data.model

/**
 * Pre-defined common foods for the search database.
 */
data class CommonFood(
    val name: String,
    val calories: Int,
    val fats: Float,
    val proteins: Float,
    val carbs: Float,
    val emoji: String = "🍽️",
    val servingSize: String = "1 serving"
) {
    fun toFoodEntry(date: java.time.LocalDate): FoodEntry {
        return FoodEntry(
            name = name,
            calories = calories,
            fats = fats,
            proteins = proteins,
            carbs = carbs,
            emoji = emoji,
            date = date,
            source = FoodSource.DATABASE
        )
    }
}

/**
 * Pre-populated database of common foods with emojis.
 */
object CommonFoodsDatabase {
    val foods = listOf(
        // Fruits
        CommonFood("Banana", 105, 0.4f, 1.3f, 27f, "🍌", "1 medium"),
        CommonFood("Apple", 95, 0.3f, 0.5f, 25f, "🍎", "1 medium"),
        CommonFood("Orange", 62, 0.2f, 1.2f, 15f, "🍊", "1 medium"),
        CommonFood("Strawberries", 50, 0.5f, 1f, 12f, "🍓", "1 cup"),
        CommonFood("Blueberries", 85, 0.5f, 1.1f, 21f, "🫐", "1 cup"),
        CommonFood("Grapes", 104, 0.2f, 1.1f, 27f, "🍇", "1 cup"),
        CommonFood("Watermelon", 46, 0.2f, 0.9f, 12f, "🍉", "1 cup"),
        
        // Vegetables
        CommonFood("Broccoli", 55, 0.6f, 3.7f, 11f, "🥦", "1 cup"),
        CommonFood("Carrot", 25, 0.1f, 0.6f, 6f, "🥕", "1 medium"),
        CommonFood("Spinach", 7, 0.1f, 0.9f, 1.1f, "🥬", "1 cup raw"),
        CommonFood("Tomato", 22, 0.2f, 1.1f, 4.8f, "🍅", "1 medium"),
        CommonFood("Cucumber", 16, 0.1f, 0.7f, 3.8f, "🥒", "1 cup"),
        CommonFood("Potato", 161, 0.2f, 4.3f, 37f, "🥔", "1 medium"),
        CommonFood("Sweet Potato", 103, 0.1f, 2.3f, 24f, "🍠", "1 medium"),
        
        // Proteins
        CommonFood("Chicken Breast", 165, 3.6f, 31f, 0f, "🍗", "100g"),
        CommonFood("Salmon", 208, 13f, 20f, 0f, "🐟", "100g"),
        CommonFood("Eggs", 155, 11f, 13f, 1.1f, "🥚", "2 large"),
        CommonFood("Beef Steak", 271, 19f, 26f, 0f, "🥩", "100g"),
        CommonFood("Tuna", 132, 1f, 28f, 0f, "🐟", "100g"),
        CommonFood("Shrimp", 99, 0.3f, 24f, 0.2f, "🦐", "100g"),
        CommonFood("Turkey Breast", 135, 0.7f, 30f, 0f, "🦃", "100g"),
        CommonFood("Tofu", 76, 4.8f, 8f, 1.9f, "🧈", "100g"),
        
        // Dairy
        CommonFood("Milk (Whole)", 149, 8f, 8f, 12f, "🥛", "1 cup"),
        CommonFood("Milk (Skim)", 83, 0.2f, 8.3f, 12f, "🥛", "1 cup"),
        CommonFood("Greek Yogurt", 100, 0.7f, 17f, 6f, "🥛", "170g"),
        CommonFood("Cheddar Cheese", 113, 9.3f, 7f, 0.4f, "🧀", "1 oz"),
        CommonFood("Cottage Cheese", 206, 9f, 28f, 6f, "🧀", "1 cup"),
        CommonFood("Butter", 102, 12f, 0.1f, 0f, "🧈", "1 tbsp"),
        
        // Grains & Bread
        CommonFood("White Rice", 206, 0.4f, 4.3f, 45f, "🍚", "1 cup cooked"),
        CommonFood("Brown Rice", 216, 1.8f, 5f, 45f, "🍚", "1 cup cooked"),
        CommonFood("Pasta", 220, 1.3f, 8.1f, 43f, "🍝", "1 cup cooked"),
        CommonFood("White Bread", 79, 1f, 2.7f, 15f, "🍞", "1 slice"),
        CommonFood("Whole Wheat Bread", 81, 1.1f, 4f, 14f, "🍞", "1 slice"),
        CommonFood("Oatmeal", 158, 3.2f, 5.5f, 27f, "🥣", "1 cup cooked"),
        CommonFood("Bagel", 277, 1.4f, 11f, 54f, "🥯", "1 medium"),
        
        // Snacks & Sweets
        CommonFood("Milka Chocolate", 540, 31f, 6.3f, 58f, "🍫", "100g"),
        CommonFood("Dark Chocolate", 170, 12f, 2.2f, 13f, "🍫", "1 oz"),
        CommonFood("Potato Chips", 152, 10f, 2f, 15f, "🥔", "1 oz"),
        CommonFood("Almonds", 164, 14f, 6f, 6f, "🥜", "1 oz"),
        CommonFood("Peanut Butter", 188, 16f, 8f, 6f, "🥜", "2 tbsp"),
        CommonFood("Ice Cream", 137, 7f, 2.3f, 16f, "🍦", "1/2 cup"),
        CommonFood("Cookie", 148, 7f, 1.5f, 20f, "🍪", "1 medium"),
        CommonFood("Donut", 195, 11f, 2.4f, 22f, "🍩", "1 medium"),
        
        // Beverages
        CommonFood("Coffee (Black)", 2, 0f, 0.3f, 0f, "☕", "1 cup"),
        CommonFood("Orange Juice", 112, 0.5f, 1.7f, 26f, "🧃", "1 cup"),
        CommonFood("Coca-Cola", 140, 0f, 0f, 39f, "🥤", "12 oz can"),
        CommonFood("Beer", 153, 0f, 1.6f, 13f, "🍺", "12 oz"),
        
        // Fast Food
        CommonFood("Hamburger", 295, 12f, 17f, 24f, "🍔", "1 burger"),
        CommonFood("Pizza (Pepperoni)", 298, 12f, 13f, 34f, "🍕", "1 slice"),
        CommonFood("French Fries", 365, 17f, 4f, 48f, "🍟", "medium serving"),
        CommonFood("Hot Dog", 290, 18f, 11f, 22f, "🌭", "1 with bun")
    )
    
    fun search(query: String): List<CommonFood> {
        if (query.isBlank()) return emptyList()
        return foods.filter { it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name }
    }
}
