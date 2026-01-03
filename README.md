# Food Tracking App 🥗

A cross-platform nutrition tracking app powered by Google's Gemini AI. Available for **Android** and **iOS**.

[![CI](https://github.com/demo-99/food-tracking/actions/workflows/ci.yml/badge.svg)](https://github.com/demo-99/food-tracking/actions/workflows/ci.yml)

## Features 🚀

- **AI Food Analysis**: Snap a photo or describe your meal, and Gemini AI estimates calories, macros, and weight 📸
- **Health Integration**: Syncs with Health Connect (Android) and HealthKit (iOS) 🏥
- **Smart Search**: Search a database of 1000+ common foods 🔍
- **Favorites**: Save frequent meals for quick access ⭐
- **Personalized Goals**: Set custom daily calorie and macro targets 🎯
- **Onboarding Flow**: Guided setup with nutrition profile selection

## Platforms

| Platform | Tech Stack | Status |
|----------|------------|--------|
| **Android** | Kotlin, Jetpack Compose, Room, CameraX | ✅ Ready |
| **iOS** | Swift, SwiftUI, SwiftData, HealthKit | ✅ Source Ready |

## Project Structure

```
food-tracking/
├── app/                    # Android app (Kotlin/Compose)
├── ios/FoodTracking/       # iOS app (Swift/SwiftUI)
├── gradle/                 # Gradle configuration
└── .github/workflows/      # CI/CD pipelines
```

## Setup & Build

### Android 📱

1. Clone the repository
2. Open in Android Studio
3. Build: `./gradlew assembleDebug`
4. Run on Android 10+ device

### iOS 🍎

1. Clone the repository
2. Open `ios/FoodTracking/` in Xcode
3. Create a new SwiftUI + SwiftData project and copy the Swift files
4. Enable HealthKit capability
5. Build and run on iOS 17+ device

### Gemini API Key 🔑

Both apps require a Gemini API key:
1. Get your key from [Google AI Studio](https://ai.google.dev)
2. Enter the key in **Settings** within the app

## CI/CD

GitHub Actions automatically build and validate both platforms:

- **Android**: Builds APK on every push/PR
- **iOS**: Validates Swift syntax on macOS runner

## Tech Stack

### Android
- Kotlin & Jetpack Compose (Material 3)
- Room Database
- Health Connect API
- CameraX
- Coroutines & Flow

### iOS
- Swift & SwiftUI
- SwiftData
- HealthKit
- PhotosPicker / AVCaptureSession

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
