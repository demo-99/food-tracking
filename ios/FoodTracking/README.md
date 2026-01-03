# iOS Food Tracking App

A native SwiftUI iOS app that mirrors the Android Food Tracking app's functionality and UI.

## Project Structure

```
FoodTracking/
├── FoodTrackingApp.swift       # App entry point
├── ContentView.swift           # Main navigation container
├── Info.plist                  # App configuration & permissions
├── FoodTracking.entitlements   # HealthKit entitlements
├── Models/
│   ├── FoodEntry.swift         # SwiftData model for food entries
│   ├── FavoriteFood.swift      # SwiftData model for favorites
│   ├── CommonFood.swift        # Static database of common foods
│   └── FoodAnalysisResult.swift # AI analysis result model
├── Views/
│   ├── TodayView.swift         # Main dashboard with daily progress
│   ├── AddFoodView.swift       # Multi-tab food entry (Photo, Describe, Favorites, Search, Manual)
│   ├── HistoryView.swift       # Past food entries by date
│   ├── SettingsView.swift      # App configuration
│   ├── OnboardingView.swift    # First-time user setup
│   └── Components/
│       └── Theme.swift         # Color and styling constants
└── Services/
    ├── GeminiService.swift     # AI food analysis via Gemini API
    ├── HealthKitManager.swift  # HealthKit integration
    └── SettingsManager.swift   # User preferences storage
```

## Features

- **AI Food Analysis**: Take a photo or describe your food, and Gemini AI estimates the nutritional values
- **Multiple Input Methods**: Photo, description, favorites, search, or manual entry
- **Nutrition Tracking**: Track calories, protein, carbs, and fat
- **Daily Goals**: Customizable nutrition goals based on your profile
- **HealthKit Integration**: Sync food entries with the Health app
- **History View**: Browse past entries grouped by date
- **Favorites**: Quick access to frequently eaten foods

## Requirements

- iOS 17.0+
- Xcode 15.0+
- A Mac for building and running

## Setup

1. **Open in Xcode**: Open `FoodTracking.xcodeproj` (or create a new project and add these files)
2. **Get Gemini API Key**: Visit [ai.google.dev](https://ai.google.dev) to get your API key
3. **Configure Capabilities**: Enable HealthKit in Signing & Capabilities
4. **Build and Run**: Select your device/simulator and build

## Creating the Xcode Project

Since you're on Linux, you'll need to create the Xcode project on a Mac:

1. Open Xcode → Create New Project → iOS → App
2. Product Name: `FoodTracking`
3. Team: Your development team
4. Organization Identifier: `com.example`
5. Interface: SwiftUI
6. Language: Swift
7. Storage: SwiftData

Then:
1. Delete the auto-generated `ContentView.swift`
2. Copy all the `.swift` files from this directory into the project
3. Add the `Info.plist` entries for permissions
4. Enable HealthKit capability and add the entitlements

## Android Counterpart

This iOS app mirrors the functionality of the Android app in the parent directory. Both apps provide:
- Same features and UI patterns
- Same nutrition database
- Same AI integration (Gemini)
- Same health platform integration (HealthKit vs Health Connect)

## CI/CD

GitHub Actions are configured for both platforms:

- **Android**: Builds on `ubuntu-latest`, produces `app-debug.apk`
- **iOS**: Runs on `macos-14` with Xcode 15, validates Swift syntax

The iOS source code is packaged as an artifact that can be downloaded and opened in Xcode on a Mac for a full build.

### Why No iOS Binary?

Apple requires:
1. **Xcode on macOS** - Cannot build on Linux
2. **Signing Certificate** - For device/App Store deployment
3. **Provisioning Profile** - Tied to your Apple Developer account

The GitHub Actions workflow validates the Swift code and packages it for you to complete the build on your Mac.
