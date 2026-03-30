# TripGenie — Android Frontend

Modern AI-powered travel planning Android app built with Kotlin, Jetpack Compose, and MVVM architecture. All AI features are accessed via the backend REST API, and no Gemini API key is used directly in the app.


## Local Development Setup

1. **Install Prerequisites**
   - Android Studio (Hedgehog 2023.1 or newer)
   - JDK 11 or higher
   - Internet connection (for Gradle sync and backend API access)

2. **Clone the Repository**
   ```bash
   git clone https://github.com/sheethalkaran/TripGenie.git
   cd TripGenie/frontend
   ```

3. **Set the Backend URL**
   - Open `app/src/main/java/com/tripgenie/data/repository/GeminiRepository.kt` and update:
   ```kotlin
   private val BASE_URL = "https://YOUR-RENDER-URL.onrender.com"
   ```
   - For local development:
   ```kotlin
   private val BASE_URL = "http://10.0.2.2:8000"  // Android emulator → localhost
   ```

4. **Open in Android Studio**
   - File → Open → select the `frontend/` folder
   - Wait for Gradle sync to complete (downloads dependencies)

5. **Configure Emulator**
   - Tools → Device Manager → Create Virtual Device
   - Recommended: Pixel 6 or Pixel 7 with API 34 (Android 14)
   - Press Run

## Project Structure

```
frontend/
└── app/src/main/java/com/tripgenie/
    ├── MainActivity.kt          # Entry point and navigation host
    ├── data/
    │   ├── models/              # Data classes: ItineraryResult, FoodItem, etc.
    │   └── repository/
    │       └── GeminiRepository.kt  # Backend HTTP calls (OkHttp)
    ├── viewmodel/
    │   └── MainViewModel.kt     # App state, business logic, StateFlows
    └── ui/
        ├── screens/
        │   ├── ItineraryScreen.kt   # Itinerary form and results
        │   ├── PackingScreen.kt     # Packing checklist with progress
        │   ├── FoodScreen.kt        # Local food recommendations grid
        │   ├── RouteMapScreen.kt    # Map view for itinerary locations
        │   └── SavedTripsScreen.kt  # Offline saved trip management
        ├── components/
        │   ├── Chatbot.kt           # Concierge Genie chat overlay
        │   ├── NavBar.kt            # Bottom navigation bar
        │   └── SharedComponents.kt  # Reusable UI components
        └── theme/
            └── Theme.kt             # App colors and typography
```

## Architecture — MVVM + Repository Pattern

```
UI (Compose Screens)
       |  user actions
       v
ViewModel (MainViewModel)
       |  repository calls
       v
Repository (GeminiRepository)
       |  HTTP POST via OkHttp
       v
FastAPI Backend
```

**UI Layer** — Compose screens observe StateFlow from the ViewModel with no business logic.

**ViewModel Layer** — `MainViewModel` holds all UI state as `StateFlow<UiState<T>>` with four states: `Idle`, `Loading`, `Success`, and `Error`. Handles form updates, triggers API calls, and manages saved trips.

**Repository Layer** — `GeminiRepository` is the single point for all backend communication. It sends POST requests, parses JSON responses, and throws typed exceptions on failure.

**Data Layer** — Kotlin data classes mirror the backend Pydantic models exactly, ensuring consistent serialization.


## Key Screens

**Itinerary** — User enters destination, days, budget, traveler type, and interests. The app calls `/itinerary` and displays a day-by-day timeline with activity times, locations, and costs.

**Packing** — Calls `/packing` and renders a categorized checklist (Clothing, Footwear, Toiletries, Electronics, Documents, Health) with a live progress bar showing the percentage packed.

**Food** — Calls `/food` and shows a 2-column card grid with dish name, restaurant, description, and best spot for the selected region. "Load More" fetches additional dishes excluding already shown ones.

**Chatbot** — Floating overlay accessible from any screen. Sends message history to `/chat` and displays the conversation with Concierge Genie.

**Saved Trips** — Persists generated itineraries to `SharedPreferences` as JSON. No internet needed to view saved trips.


## Troubleshooting

| Issue                    | Fix                                                             |
|--------------------------|------------------------------------------------------------------|
| Network error on emulator| Use `http://10.0.2.2:8000` instead of `localhost`               |
| Gradle sync fails        | File → Invalidate Caches / Restart                              |
| `mipmap/ic_launcher not found` | Ensure all mipmap XML files are present in each density folder |
| Slow first response      | Free Render tier sleeps after inactivity (first request ~30s)   |

For backend setup, see [backend/README.md](../backend/README.md).
