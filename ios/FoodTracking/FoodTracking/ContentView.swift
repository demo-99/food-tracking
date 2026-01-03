// ContentView - Main navigation container

import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0
    @State private var showOnboarding = !UserDefaults.standard.bool(forKey: "onboardingComplete")
    
    var body: some View {
        Group {
            if showOnboarding {
                OnboardingView(isComplete: $showOnboarding)
            } else {
                TabView(selection: $selectedTab) {
                    TodayView()
                        .tabItem {
                            Label("Today", systemImage: "calendar")
                        }
                        .tag(0)
                    
                    HistoryView()
                        .tabItem {
                            Label("History", systemImage: "clock.arrow.circlepath")
                        }
                        .tag(1)
                    
                    SettingsView()
                        .tabItem {
                            Label("Settings", systemImage: "gearshape")
                        }
                        .tag(2)
                }
            }
        }
    }
}

#Preview {
    ContentView()
        .modelContainer(for: [FoodEntry.self, FavoriteFood.self], inMemory: true)
}
