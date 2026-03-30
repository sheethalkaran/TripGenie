package com.tripgenie.data.models

import com.google.gson.Gson

// ─── Itinerary Models ────────────────────────────────────────────────
data class ItineraryRequest(
    val destination: String,
    val durationDays: Int,
    val budget: String,
    val travelerType: String,
    val interests: String,
    val useGrounding: Boolean = false
)

data class ItineraryResult(
    val title: String,
    val description: String,
    val days: List<DayPlan>,
    val budgetBreakdown: BudgetBreakdown,
    val localFoods: List<String>,
    val recommendedRestaurants: List<String>
) {
    // Serialize to JSON string for saving
    fun toJson(): String = Gson().toJson(this)

    // Build plain text for copy
    fun toCopyText(): String {
        val sb = StringBuilder()
        sb.appendLine("✈️ $title")
        sb.appendLine(description)
        sb.appendLine()
        days.forEach { day ->
            sb.appendLine("━━━ Day ${day.dayNumber}: ${day.theme} ━━━")
            day.activities.forEach { act ->
                sb.appendLine("  ${act.time} — ${act.title}")
                sb.appendLine("    ${act.description}")
                if (act.location.isNotBlank()) sb.appendLine("    📍 ${act.location}")
                if (act.estimatedCost.isNotBlank()) sb.appendLine("    💰 ${act.estimatedCost}")
            }
            sb.appendLine()
        }
        sb.appendLine("🍜 Local Foods: ${localFoods.joinToString(", ")}")
        sb.appendLine("🍽️ Restaurants: ${recommendedRestaurants.joinToString(", ")}")
        return sb.toString()
    }

    companion object {
        fun fromJson(json: String): ItineraryResult = Gson().fromJson(json, ItineraryResult::class.java)
    }
}

data class DayPlan(
    val dayNumber: Int,
    val theme: String,
    val activities: List<Activity>
)

data class Activity(
    val time: String,
    val title: String,
    val description: String,
    val location: String = "",
    val estimatedCost: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class BudgetBreakdown(
    val accommodation: Int,
    val activities: Int,
    val food: Int,
    val transport: Int
)

// ─── Saved Trip ───────────────────────────────────────────────────────
data class SavedTrip(
    val id: String,
    val savedAt: Long,
    val resultJson: String  // ItineraryResult serialized
) {
    fun toResult(): ItineraryResult = ItineraryResult.fromJson(resultJson)
}

// ─── Packing Models ──────────────────────────────────────────────────
data class PackingRequest(
    val destination: String,
    val weatherType: String,
    val tripDuration: Int,
    val mainActivities: String
)

data class PackingResult(val categories: List<PackingCategory>)
data class PackingCategory(val name: String, val icon: String, val items: List<PackingItem>)
data class PackingItem(val name: String, var isPacked: Boolean = false)

// ─── Food Models ─────────────────────────────────────────────────────
data class FoodItem(
    val name: String,
    val restaurant: String,
    val description: String,
    val bestSpot: String,
    val icon: String = "🍽️"
)

// ─── Chat Models ─────────────────────────────────────────────────────
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── UI State ────────────────────────────────────────────────────────
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
