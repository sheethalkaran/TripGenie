package com.tripgenie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripgenie.ui.components.*
import com.tripgenie.ui.screens.*
import com.tripgenie.ui.theme.*
import com.tripgenie.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { TripGenieTheme { TripGenieApp() } }
    }
}

@Composable
fun TripGenieApp(vm: MainViewModel = viewModel()) {
    val selectedTab      by vm.selectedTab.collectAsStateWithLifecycle()
    val itineraryState   by vm.itineraryState.collectAsStateWithLifecycle()
    val itineraryRequest by vm.itineraryRequest.collectAsStateWithLifecycle()
    val packingState     by vm.packingState.collectAsStateWithLifecycle()
    val packingRequest   by vm.packingRequest.collectAsStateWithLifecycle()
    val foodState        by vm.foodState.collectAsStateWithLifecycle()
    val foodLoadMore     by vm.foodLoadMoreState.collectAsStateWithLifecycle()
    val foodItems        by vm.foodItems.collectAsStateWithLifecycle()
    val foodRegion       by vm.foodRegion.collectAsStateWithLifecycle()
    val chatMessages     by vm.chatMessages.collectAsStateWithLifecycle()
    val chatLoading      by vm.chatLoading.collectAsStateWithLifecycle()
    val savedTrips       by vm.savedTrips.collectAsStateWithLifecycle()

    var showSavedTrips by remember { mutableStateOf(false) }
    var showRouteMap   by remember { mutableStateOf(false) }

    // Overlay screens (full screen replacements)
    when {
        showRouteMap -> {
            val result = (itineraryState as? com.tripgenie.data.models.UiState.Success)?.data
            if (result != null) {
                RouteMapScreen(result = result, onBack = { showRouteMap = false })
            } else {
                showRouteMap = false
            }
            return
        }
        showSavedTrips -> {
            SavedTripsScreen(
                savedTrips = savedTrips,
                onViewTrip = { trip ->
                    vm.viewSavedTrip(trip)
                    showSavedTrips = false
                },
                onDeleteTrip = { vm.deleteSavedTrip(it) },
                onClose = { showSavedTrips = false }
            )
            return
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky top
            Column(modifier = Modifier.fillMaxWidth()) {
                TripGenieNavBar(
                    savedCount = savedTrips.size,
                    onSavedTripsClick = { showSavedTrips = true }
                )
                HeroSection(selectedTab = selectedTab, onTabSelected = { vm.selectTab(it) })
            }

            // Scrollable content
            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = selectedTab, animationSpec = tween(250), label = "tabs") { tab ->
                    when (tab) {
                        0 -> ItineraryScreen(
                            itineraryState       = itineraryState,
                            request              = itineraryRequest,
                            onDestinationChange  = { vm.updateItineraryDestination(it) },
                            onDurationChange     = { vm.updateItineraryDuration(it) },
                            onBudgetChange       = { vm.updateItineraryBudget(it) },
                            onTravelerTypeChange = { vm.updateItineraryTravelerType(it) },
                            onInterestsChange    = { vm.updateItineraryInterests(it) },
                            onGroundingChange    = { vm.updateItineraryGrounding(it) },
                            onGenerate           = { vm.generateItinerary() },
                            onReset              = { vm.resetItinerary() },
                            onSaveTrip           = { vm.saveCurrentTrip() },
                            onShowRouteMap       = { showRouteMap = true }
                        )
                        1 -> PackingScreen(
                            packingState        = packingState,
                            request             = packingRequest,
                            checkedItems        = vm.checkedItems,
                            onDestinationChange = { vm.updatePackingDestination(it) },
                            onWeatherChange     = { vm.updatePackingWeather(it) },
                            onDurationChange    = { vm.updatePackingDuration(it) },
                            onActivitiesChange  = { vm.updatePackingActivities(it) },
                            onGenerate          = { vm.generatePackingList() },
                            onToggleItem        = { vm.togglePackedItem(it) },
                            getTotalItems       = { vm.getTotalItems(it) },
                            getPackedCount      = { vm.getPackedCount() },
                            onReset             = { vm.resetPacking() }
                        )
                        2 -> FoodScreen(
                            foodState         = foodState,
                            foodLoadMoreState = foodLoadMore,
                            foodItems         = foodItems,
                            region            = foodRegion,
                            onRegionChange    = { vm.updateFoodRegion(it) },
                            onFetch           = { vm.fetchFoodInsights() },
                            onLoadMore        = { vm.loadMoreFoods() },
                            onReset           = { vm.resetFood() }
                        )
                    }
                }
            }
        }
        FloatingChatbot(messages = chatMessages, isLoading = chatLoading, onSendMessage = { vm.sendChatMessage(it) })
    }
}

@Composable
fun HeroSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF2D4DE0), Color(0xFF1EB8A6))))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Column {
            Text("Plan Your Journey.\nNo Delays.", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, lineHeight = 30.sp)
            Spacer(Modifier.height(3.dp))
            Text("Lightning-fast AI itineraries and professional packing audits.",
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), lineHeight = 17.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.15f)).padding(3.dp)
            ) {
                listOf("ITINERARY", "PACKING", "FOOD").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                            .background(if (i == selectedTab) Color.White else Color.Transparent)
                            .clickable { onTabSelected(i) }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 11.sp,
                            fontWeight = if (i == selectedTab) FontWeight.Bold else FontWeight.Medium,
                            color = if (i == selectedTab) PrimaryBlue else Color.White,
                            letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}
