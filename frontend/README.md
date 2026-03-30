# TripGenie Frontend — Android App

TripGenie is a modern AI-powered travel planning Android app built with Kotlin, Jetpack Compose, and MVVM architecture. All AI features are accessed via the backend REST API; no Gemini API key is used directly in the app.

## Local Development Setup

1. **Install Prerequisites**
   - Android Studio (Hedgehog 2023.1 or newer)
   - JDK 11 or higher
   - Internet connection (for Gradle sync and backend API access)
2. **Clone the Repository**
   - `git clone <repo-url>`
3. **Open the Project**
   - Launch Android Studio
   - Select `File → Open` and choose the `TripGenie` folder
4. **Sync and Build**
   - Wait for Gradle sync to complete (downloads dependencies)
   - If prompted, click "Trust Project"
5. **Configure Emulator**
   - Tools → Device Manager → Create Virtual Device
   - Recommended: Pixel 6 or Pixel 7, API 34 (Android 14)
   - Download system image if needed
6. **Run the App**
   - Select the emulator and press the Run button

## Project Structure

```
frontend/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/tripgenie/
│   │       │   ├── MainActivity.kt
│   │       │   ├── data/
│   │       │   │   ├── models/           # Data classes for API and UI
│   │       │   │   └── repository/       # GeminiRepository: all backend API calls
│   │       │   ├── viewmodel/            # MainViewModel: app state & business logic
│   │       │   └── ui/                   # Compose UI: screens, components, theme
│   │       └── res/                      # Resources: layouts, drawables, values
│   ├── build.gradle.kts                  # App-level Gradle config
│   └── proguard-rules.pro
├── build.gradle.kts                      # Root Gradle config
├── gradle/                               # Gradle wrapper and version catalog
└── settings.gradle.kts
```

## Architecture (MVVM + Repository)

- **UI Layer (Compose):**
  - Screens and components in `ui/` display data and handle user input.
  - All UI observes state from the ViewModel using StateFlow.
- **ViewModel Layer:**
  - `MainViewModel` holds all UI state, exposes StateFlows for each feature (itinerary, packing, food, chat).
  - Handles user actions, form updates, and triggers repository calls.
- **Repository Layer:**
  - `GeminiRepository` is the single source for all backend API calls (itinerary, packing, food, chat).
  - Uses OkHttp for HTTP requests and parses JSON responses.
- **Data Layer:**
  - Data classes in `models/` represent requests and responses for each feature.

**Flow:**
1. User interacts with a screen (e.g., submits itinerary form)
2. UI calls a function on `MainViewModel`
3. ViewModel updates state and calls the appropriate repository method
4. Repository sends HTTP request to backend, parses response
5. ViewModel updates StateFlow with result (Idle → Loading → Success/Error)
6. UI observes StateFlow and updates the screen accordingly

## Key Features

- Generate multi-day trip itineraries with activities, locations, and budget
- Create packing lists tailored to weather and activities
- Discover authentic local foods and restaurants
- Chatbot assistant for travel tips and Q&A

## Troubleshooting

- If you see `mipmap/ic_launcher not found`, ensure all mipmap XMLs are present in each mipmap-*dpi folder
- For network/API errors, check your backend server is running and accessible
- For Gradle sync issues, try File → Invalidate Caches / Restart

---

For backend setup and deployment, see `../backend/README.md`.
