# ✈️ TripGenie — AI Travel Planning App

A modern AI-powered travel planning Android app built with **Kotlin**, **Jetpack Compose**, **Google Gemini API**, and **MVVM Architecture**.

---

## 🚀 Quick Setup (5 steps)

### Step 1 — Prerequisites
- Android Studio **Hedgehog (2023.1)** or newer
- JDK 11+
- Internet connection (for Gradle sync and Gemini API)

### Step 2 — Get a Gemini API Key
1. Go to → https://aistudio.google.com/app/apikey
2. Click **"Create API Key"**
3. Copy the key

### Step 3 — Add Your API Key
Open this file:
```
app/src/main/java/com/tripgenie/viewmodel/MainViewModel.kt
```
Find this line (~line 13):
```kotlin
private val repository = GeminiRepository(apiKey = "YOUR_GEMINI_API_KEY_HERE")
```
Replace `YOUR_GEMINI_API_KEY_HERE` with your actual key.

### Step 4 — Open in Android Studio
1. Open Android Studio
2. **File → Open** → select the `TripGenie` folder
3. Wait for Gradle sync to complete (downloads ~200MB first time)
4. If prompted: **"Trust Project"** → click Yes

### Step 5 — Run the App
**Recommended Emulator:**
- Device: **Pixel 6** or **Pixel 7**
- API Level: **API 34 (Android 14)**
- Why: Best Jetpack Compose performance, adaptive icon support, fast rendering

To create the emulator in Android Studio:
1. Tools → Device Manager → Create Virtual Device
2. Select **Pixel 6** → Next
3. Select **API 34** → Download if needed → Next → Finish
4. Press ▶ Run button

---

## 📁 Full Project Structure

```
TripGenie/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml           # App permissions + entry activity
│   │       ├── java/com/tripgenie/
│   │       │   │
│   │       │   ├── MainActivity.kt           # ★ App entry point
│   │       │   │   • ComponentActivity setup
│   │       │   │   • TripGenieApp() root composable
│   │       │   │   • HeroSection() with animated headline
│   │       │   │   • Tab Crossfade navigation
│   │       │   │   • Connects ViewModel → Screens
│   │       │   │
│   │       │   ├── data/
│   │       │   │   ├── models/
│   │       │   │   │   └── Models.kt         # ★ All data classes
│   │       │   │   │       • ItineraryRequest / ItineraryResult
│   │       │   │   │       • DayPlan / Activity
│   │       │   │   │       • BudgetBreakdown
│   │       │   │   │       • PackingRequest / PackingResult
│   │       │   │   │       • PackingCategory / PackingItem
│   │       │   │   │       • FoodItem
│   │       │   │   │       • ChatMessage
│   │       │   │   │       • UiState<T> sealed class
│   │       │   │   │
│   │       │   │   └── repository/
│   │       │   │       └── GeminiRepository.kt  # ★ All AI API calls
│   │       │   │           • generateItinerary() → Gemini JSON prompt
│   │       │   │           • generatePackingList() → Gemini JSON prompt
│   │       │   │           • generateFoodInsights() → Gemini JSON prompt
│   │       │   │           • chat() → Concierge Genie chatbot
│   │       │   │           • JSON parsing for each response type
│   │       │   │
│   │       │   ├── viewmodel/
│   │       │   │   └── MainViewModel.kt      # ★ MVVM State Hub
│   │       │   │       • selectedTab: StateFlow<Int>
│   │       │   │       • itineraryState: StateFlow<UiState<ItineraryResult>>
│   │       │   │       • packingState: StateFlow<UiState<PackingResult>>
│   │       │   │       • foodState / foodItems / foodLoadMoreState
│   │       │   │       • chatMessages / chatLoading
│   │       │   │       • checkedItems: mutableStateListOf (packing checkboxes)
│   │       │   │       • All form field update functions
│   │       │   │       • generateItinerary() / generatePackingList() / etc.
│   │       │   │       • loadMoreFoods() — tracks shown foods, prevents repeats
│   │       │   │
│   │       │   └── ui/
│   │       │       ├── theme/
│   │       │       │   └── Theme.kt          # ★ Design system
│   │       │       │       • Brand colors (PrimaryBlue, AccentTeal, etc.)
│   │       │       │       • Chart colors (4 segments)
│   │       │       │       • Day route colors (6 colors)
│   │       │       │       • Gradient brushes (AppBackground, Button, Hero)
│   │       │       │       • MaterialTheme typography scale
│   │       │       │       • TripGenieTheme() wrapper
│   │       │       │
│   │       │       ├── components/
│   │       │       │   ├── SharedComponents.kt  # ★ Reusable components
│   │       │       │   │   • GradientBackground()
│   │       │       │   │   • PrimaryButton() — animated gradient button
│   │       │       │   │   • SectionCard() — white rounded card
│   │       │       │   │   • TripInput() — styled OutlinedTextField
│   │       │       │   │   • TripDropdown() — styled ExposedDropdownMenu
│   │       │       │   │   • SegmentedControl() — pill tab switcher
│   │       │       │   │   • LoadingModal() — animated airplane loading overlay
│   │       │       │   │   • DonutChart() — canvas-drawn budget chart
│   │       │       │   │   • ErrorCard() — retry error display
│   │       │       │   │   • SectionHeader() — title + subtitle
│   │       │       │   │   • RestaurantBadge() — blue pill badge
│   │       │       │   │
│   │       │       │   ├── NavBar.kt            # ★ Top navigation bar
│   │       │       │   │   • Logo (T icon + TripGenie name)
│   │       │       │   │   • Center tab navigation (ITINERARY/PACKING/FOODIE)
│   │       │       │   │   • Member Portal button (right)
│   │       │       │   │
│   │       │       │   └── Chatbot.kt           # ★ Floating AI assistant
│   │       │       │       • FloatingChatbot() — expandable FAB chat window
│   │       │       │       • ChatBubble() — user/AI message bubbles
│   │       │       │       • TypingIndicator() — animated dots
│   │       │       │
│   │       │       └── screens/
│   │       │           ├── ItineraryScreen.kt   # ★ Itinerary feature
│   │       │           │   • ItineraryScreen() — form with all inputs
│   │       │           │   • ItineraryResultScreen() — timeline + sidebar
│   │       │           │   • DayTimeline() — day-by-day activity cards
│   │       │           │   • ActionChip() — PDF/Save/Copy buttons
│   │       │           │
│   │       │           ├── PackingScreen.kt     # ★ Packing feature
│   │       │           │   • PackingScreen() — form + results
│   │       │           │   • PackingCategoryCard() — collapsible category
│   │       │           │   • Progress bar with % complete
│   │       │           │   • Strikethrough checked items
│   │       │           │
│   │       │           └── FoodScreen.kt        # ★ Food discovery feature
│   │       │               • FoodScreen() — region input + grid
│   │       │               • FoodCard() — food card with hover animation
│   │       │               • Load More Foods (appends, no repeats)
│   │       │               • 2-column responsive grid
│   │       │
│   │       └── res/
│   │           ├── drawable/
│   │           │   ├── ic_launcher_background.xml  # Blue→Teal gradient
│   │           │   └── ic_launcher_foreground.xml  # Airplane vector icon
│   │           ├── mipmap-mdpi/
│   │           │   ├── ic_launcher.xml             # Adaptive icon
│   │           │   └── ic_launcher_round.xml
│   │           ├── mipmap-hdpi/ (same)
│   │           ├── mipmap-xhdpi/ (same)
│   │           ├── mipmap-xxhdpi/ (same)
│   │           ├── mipmap-xxxhdpi/ (same)
│   │           └── values/
│   │               ├── strings.xml                 # App name
│   │               └── themes.xml                  # Base theme
│   │
│   ├── build.gradle.kts                    # App-level dependencies
│   └── proguard-rules.pro
│
├── gradle/
│   ├── libs.versions.toml                  # ★ All dependency versions (TOML)
│   └── wrapper/
│       └── gradle-wrapper.properties       # Gradle 8.9
│
├── build.gradle.kts                        # Root build file
└── settings.gradle.kts                     # Module includes

```

---

## 🏗️ MVVM Architecture Flow

```
User Action
    │
    ▼
[UI Screen / Composable]
    │  calls function on
    ▼
[MainViewModel]                        ← Single ViewModel for all screens
    │  updates StateFlow
    │  calls suspend fun in
    ▼
[GeminiRepository]                     ← All AI API calls here
    │  sends prompt to
    ▼
[Google Gemini API]                    ← Returns JSON string
    │
    ▼
[GeminiRepository parses JSON]         ← org.json parsing
    │  returns data class
    ▼
[MainViewModel updates UiState]        ← Idle → Loading → Success/Error
    │
    ▼
[UI observes StateFlow]                ← collectAsStateWithLifecycle()
    │
    ▼
[Composable recomposes]                ← Shows result / error / loading
```

---

## 🤖 AI Generation (100% Dynamic, No Hardcoded Content)

### Itinerary Generation
- Sends destination, duration, budget, traveler type, interests to Gemini
- Receives JSON with: title, description, days[], activities[], budget breakdown, local foods, restaurants
- Each activity includes realistic lat/lng coordinates

### Packing List Generation
- Sends destination, weather type, duration, activities to Gemini
- Receives JSON with 6+ categories, each with 6-10 specific items
- Items are weather and activity appropriate (e.g. "Waterproof trekking boots" for hiking in rainy weather)

### Food Discovery
- Sends region name to Gemini
- Receives 6 authentic local dishes with restaurant names and descriptions
- "Load More" sends previously shown food names to Gemini with instruction to NOT repeat them

### Chatbot (Concierge Genie)
- Full conversation history sent with each message
- Responds with travel insights: airports, transport, best season, local tips

---

## 🎨 Design System

| Token | Value | Usage |
|-------|-------|-------|
| PrimaryBlue | `#2D4DE0` | Buttons, tabs, accents |
| AccentTeal | `#1EB8A6` | Icons, locations, secondary |
| Background | `#F0F4FF → #E8F5F3` | App gradient background |
| TextPrimary | `#1A1A2E` | Headings, labels |
| TextSecondary | `#6B7280` | Body text, subtitles |
| ErrorRed | `#EF4444` | Error states |
| SuccessGreen | `#10B981` | Packing complete |
| WarningOrange | `#F97316` | Load More button, costs |

---

## 🔧 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2024.11.00 | UI framework |
| Material3 | via BOM | Design components |
| Navigation Compose | 2.8.4 | Screen navigation |
| ViewModel Compose | 2.8.7 | MVVM state |
| Generative AI (Gemini) | 0.9.0 | AI content generation |
| Kotlinx Coroutines | 1.9.0 | Async operations |
| Gson / org.json | built-in | JSON parsing |

---

## ❗ Common Errors & Fixes

| Error | Fix |
|-------|-----|
| `mipmap/ic_launcher not found` | All mipmap XMLs are in each mipmap-*dpi folder — this is fixed in this project |
| `API key invalid` | Get key from https://aistudio.google.com — free tier works |
| `Network error` | Check internet on emulator: Settings → Network |
| `Gradle sync failed` | File → Invalidate Caches → Restart |
| `Build tools not found` | SDK Manager → Install "Android SDK Build-Tools 35" |

---

## 📱 Recommended Emulator

| Setting | Value |
|---------|-------|
| Device | **Pixel 6** or **Pixel 7** |
| API Level | **API 34 (Android 14, "UpsideDownCake")** |
| RAM | 4096 MB |
| Graphics | Hardware — GLES 2.0 |
| Storage | 6 GB |

Why Pixel 6/7 API 34?
- Best Compose performance (hardware acceleration)
- Supports adaptive icons (your mipmap XMLs use adaptive-icon format)
- Stable and fast cold boot (~30 seconds)
- Same screen density as most real devices (xxhdpi)

---

## 🔑 UiState Pattern

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()       // No action taken yet
    object Loading : UiState<Nothing>()   // API call in progress
    data class Success<T>(val data: T)    // API returned data
    data class Error(val message: String) // API failed
}
```

Screens observe state and render accordingly:
- **Idle** → show input form
- **Loading** → show airplane loading modal
- **Success** → show generated content with fade-in animation
- **Error** → show error card with retry button
