package com.tripgenie.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tripgenie.data.models.*
import com.tripgenie.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.core.content.edit

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("tripgenie_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // API calls go through the FastAPI backend — no key needed here
    private val repository = GeminiRepository()

    // ── Tab ──────────────────────────────────────────────────────────
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    fun selectTab(i: Int) { _selectedTab.value = i }

    // ── Shared destination + duration synced across screens ──────────
    private val _sharedDestination = MutableStateFlow("")
    private val _sharedDuration    = MutableStateFlow(0)

    // ── Itinerary ────────────────────────────────────────────────────
    private val _itineraryState = MutableStateFlow<UiState<ItineraryResult>>(UiState.Idle)
    val itineraryState: StateFlow<UiState<ItineraryResult>> = _itineraryState.asStateFlow()

    private val _itineraryRequest = MutableStateFlow(ItineraryRequest("", 0, "Moderate", "Solo", ""))
    val itineraryRequest: StateFlow<ItineraryRequest> = _itineraryRequest.asStateFlow()

    fun updateItineraryDestination(v: String) {
        _itineraryRequest.value = _itineraryRequest.value.copy(destination = v)
        _sharedDestination.value = v
        // Always sync to packing and food so they stay in lockstep while user types
        _packingRequest.value = _packingRequest.value.copy(destination = v)
        _foodRegion.value = v
    }

    fun updateItineraryDuration(v: Int) {
        _itineraryRequest.value = _itineraryRequest.value.copy(durationDays = v)
        _sharedDuration.value = v
        // Always sync duration to packing too
        _packingRequest.value = _packingRequest.value.copy(tripDuration = v)
    }

    fun updateItineraryBudget(v: String)      { _itineraryRequest.value = _itineraryRequest.value.copy(budget = v) }
    fun updateItineraryTravelerType(v: String){ _itineraryRequest.value = _itineraryRequest.value.copy(travelerType = v) }
    fun updateItineraryInterests(v: String)   { _itineraryRequest.value = _itineraryRequest.value.copy(interests = v) }
    fun updateItineraryGrounding(v: Boolean)  { _itineraryRequest.value = _itineraryRequest.value.copy(useGrounding = v) }

    fun generateItinerary() {
        val req = _itineraryRequest.value
        if (req.destination.isBlank() || req.durationDays <= 0) return
        _itineraryState.value = UiState.Loading
        viewModelScope.launch {
            try {
                _itineraryState.value = UiState.Success(repository.generateItinerary(req))
            } catch (e: Exception) {
                _itineraryState.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun resetItinerary() { _itineraryState.value = UiState.Idle }

    // ── Saved Trips ──────────────────────────────────────────────────
    private val _savedTrips = MutableStateFlow<List<SavedTrip>>(emptyList())
    val savedTrips: StateFlow<List<SavedTrip>> = _savedTrips.asStateFlow()

    private val _viewingSavedTrip = MutableStateFlow<ItineraryResult?>(null)
    val viewingSavedTrip: StateFlow<ItineraryResult?> = _viewingSavedTrip.asStateFlow()

    init {
        loadSavedTrips()
        viewModelScope.launch {
            try { repository.pingServer() } catch (_: Exception) {}
        }
    }

    private fun loadSavedTrips() {
        val json = prefs.getString("saved_trips", "[]") ?: "[]"
        val type = object : TypeToken<List<SavedTrip>>() {}.type
        _savedTrips.value = runCatching { gson.fromJson<List<SavedTrip>>(json, type) }.getOrElse { emptyList() }
    }

    fun saveCurrentTrip(): Boolean {
        val result = (_itineraryState.value as? UiState.Success)?.data ?: return false
        val trip = SavedTrip(
            id = UUID.randomUUID().toString(),
            savedAt = System.currentTimeMillis(),
            resultJson = result.toJson()
        )
        val updated = _savedTrips.value + trip
        _savedTrips.value = updated
        prefs.edit { putString("saved_trips", gson.toJson(updated)) }
        return true
    }

    fun deleteSavedTrip(id: String) {
        val updated = _savedTrips.value.filter { it.id != id }
        _savedTrips.value = updated
        prefs.edit().putString("saved_trips", gson.toJson(updated)).apply()
    }

    fun viewSavedTrip(trip: SavedTrip) {
        _viewingSavedTrip.value = trip.toResult()
        selectTab(0)
        _itineraryState.value = UiState.Success(trip.toResult())
    }

    fun clearViewingSavedTrip() { _viewingSavedTrip.value = null }

    // ── Packing ──────────────────────────────────────────────────────
    private val _packingState = MutableStateFlow<UiState<PackingResult>>(UiState.Idle)
    val packingState: StateFlow<UiState<PackingResult>> = _packingState.asStateFlow()

    private val _packingRequest = MutableStateFlow(PackingRequest("", "Mixed", 0, ""))
    val packingRequest: StateFlow<PackingRequest> = _packingRequest.asStateFlow()

    val checkedItems = mutableStateListOf<String>()

    fun updatePackingDestination(v: String) { _packingRequest.value = _packingRequest.value.copy(destination = v) }
    fun updatePackingWeather(v: String)     { _packingRequest.value = _packingRequest.value.copy(weatherType = v) }
    fun updatePackingDuration(v: Int)       { _packingRequest.value = _packingRequest.value.copy(tripDuration = v) }
    fun updatePackingActivities(v: String)  { _packingRequest.value = _packingRequest.value.copy(mainActivities = v) }

    fun generatePackingList() {
        val req = _packingRequest.value
        if (req.destination.isBlank()) return
        _packingState.value = UiState.Loading
        checkedItems.clear()
        viewModelScope.launch {
            try {
                _packingState.value = UiState.Success(repository.generatePackingList(req))
            } catch (e: Exception) {
                _packingState.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun togglePackedItem(key: String) {
        if (checkedItems.contains(key)) checkedItems.remove(key) else checkedItems.add(key)
    }

    fun getTotalItems(r: PackingResult) = r.categories.sumOf { it.items.size }
    fun getPackedCount() = checkedItems.size
    fun resetPacking() { _packingState.value = UiState.Idle }

    // ── Food ─────────────────────────────────────────────────────────
    private val _foodState = MutableStateFlow<UiState<List<FoodItem>>>(UiState.Idle)
    val foodState: StateFlow<UiState<List<FoodItem>>> = _foodState.asStateFlow()

    private val _foodLoadMoreState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val foodLoadMoreState: StateFlow<UiState<Unit>> = _foodLoadMoreState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _foodRegion = MutableStateFlow("")
    val foodRegion: StateFlow<String> = _foodRegion.asStateFlow()

    private val shownFoodNames = mutableListOf<String>()

    fun updateFoodRegion(v: String) { _foodRegion.value = v }

    fun fetchFoodInsights() {
        val region = _foodRegion.value.ifBlank { return }
        _foodState.value = UiState.Loading
        shownFoodNames.clear(); _foodItems.value = emptyList()
        viewModelScope.launch {
            try {
                val items = repository.generateFoodInsights(region, emptyList())
                shownFoodNames.addAll(items.map { it.name })
                _foodItems.value = items
                _foodState.value = UiState.Success(items)
            } catch (e: Exception) {
                _foodState.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun loadMoreFoods() {
        val region = _foodRegion.value.ifBlank { return }
        _foodLoadMoreState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val new = repository.generateFoodInsights(region, shownFoodNames.toList())
                shownFoodNames.addAll(new.map { it.name })
                _foodItems.value += new
                _foodLoadMoreState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _foodLoadMoreState.value = UiState.Error(e.message ?: "Failed")
            }
        }
    }

    fun resetFood() { _foodState.value = UiState.Idle; _foodItems.value = emptyList() }

    // ── Chat ─────────────────────────────────────────────────────────
    private val _chatMessages = MutableStateFlow(listOf(
        ChatMessage("Greetings! I am TripGenie, your personal travel concierge. How may I assist your wanderlust today? ✈️", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        _chatMessages.value += ChatMessage(message, true)
        _chatLoading.value = true
        viewModelScope.launch {
            try {
                val reply = repository.chat(message, _chatMessages.value)
                _chatMessages.value += ChatMessage(reply, false)
            } catch (e: Exception) {
                _chatMessages.value += ChatMessage("Sorry, couldn't respond. Try again. 🙏", false)
            } finally {
                _chatLoading.value = false
            }
        }
    }
}
