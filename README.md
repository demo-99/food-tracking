# Food Tracking App 🥗

A nutrition tracking app for Android powered by Google's Gemini AI.

[![CI](https://github.com/demo-99/food-tracking/actions/workflows/ci.yml/badge.svg)](https://github.com/demo-99/food-tracking/actions/workflows/ci.yml)

## Features 🚀

- **AI Food Analysis**: Snap a photo or describe your meal, and Gemini AI estimates calories, macros, and weight 📸
- **Health Integration**: Syncs with Health Connect for Android 🏥
- **Smart Search**: Search a database of 3000+ common foods 🔍
- **Favorites**: Save frequent meals for quick access ⭐
- **Personalized Goals**: Set custom daily calorie and macro targets 🎯
- **History View**: Browse and edit food entries from previous days 📅
- **Onboarding Flow**: Guided setup with nutrition profile selection

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room
- **Health**: Health Connect API
- **Camera**: CameraX
- **Async**: Coroutines & Flow
- **AI**: Gemini API

## Project Structure

```
food-tracking/
├── app/                    # Android app source
│   └── src/main/java/      # Kotlin source files
├── gradle/                 # Gradle configuration
├── scripts/                # Build/processing scripts
└── .github/workflows/      # CI/CD pipeline
```

## Setup & Build 📱

1. Clone the repository
2. Open in Android Studio
3. Build: `./gradlew assembleDebug`
4. Run on Android 7.0+ (API 24+) device

### Gemini API Key 🔑

The app requires a Gemini API key for AI food analysis:
1. Get your key from [Google AI Studio](https://ai.google.dev)
2. Enter the key in **Settings** within the app

## CI/CD

GitHub Actions automatically builds on every push/PR:
- Builds Debug APK
- Runs unit tests
- Uploads APK as artifact

## Requirements

- Android 7.0+ (API 24+)
- Health Connect app (optional, for health data sync)

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
