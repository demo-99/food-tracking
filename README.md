# AI Food Tracking App 🥗

A modern Android application for tracking nutrition powered by Google's Gemini AI. 

## Features 🚀

*   **AI Food Analysis**: Snap a photo or describe your meal, and Gemini AI estimates calories, macros (fats, proteins, carbs), and weight. 📸
*   **Health Connect Sync**: Automatically syncs your nutrition data with Health Connect to work with other fitness apps. 🏥
*   **Smart Search**: Search a local database of common foods. 🔍
*   **Favorites**: Save your frequent meals for quick access. ⭐
*   **Personalized Goals**: Set custom daily limits for calories and macros. 🎯

## Tech Stack 🛠️

*   **Kotlin** & **Jetpack Compose** (Material 3)
*   **Gemini AI** (Generative Model)
*   **Health Connect API**
*   **Room Database**
*   **Coroutines & Flow**
*   **CameraX**

## Setup & Build 📱

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/food-tracking.git
    ```

2.  **Open in Android Studio**.

3.  **Gemini API Key**:
    *   This app requires a Gemini API key to function.
    *   Get your key from [Google AI Studio](https://makersuite.google.com/app/apikey).
    *   Once the app is running, go to **Settings** and enter your API key.

4.  **Build and Run**:
    *   Build the project using Gradle: `./gradlew assembleDebug`
    *   Run on an Android device (Android 10+ recommended for Health Connect).

## License 📄

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
