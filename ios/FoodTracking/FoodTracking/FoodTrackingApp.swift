// FoodTracking iOS App
// Main entry point

import SwiftUI
import SwiftData

@main
struct FoodTrackingApp: App {
    let modelContainer: ModelContainer
    
    init() {
        do {
            modelContainer = try ModelContainer(for: FoodEntry.self, FavoriteFood.self)
        } catch {
            fatalError("Failed to initialize model container: \(error)")
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .modelContainer(modelContainer)
        }
    }
}
