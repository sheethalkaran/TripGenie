package com.tripgenie.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.tripgenie.data.models.*
import com.tripgenie.ui.components.*
import com.tripgenie.ui.theme.*

@Composable
fun ItineraryScreen(
    itineraryState: UiState<ItineraryResult>,
    request: ItineraryRequest,
    onDestinationChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onBudgetChange: (String) -> Unit,
    onTravelerTypeChange: (String) -> Unit,
    onInterestsChange: (String) -> Unit,
    onGroundingChange: (Boolean) -> Unit,
    onGenerate: () -> Unit,
    onReset: () -> Unit,
    onSaveTrip: () -> Boolean,
    onShowRouteMap: () -> Unit
) {
    val context = LocalContext.current

    when (itineraryState) {
        is UiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingModal("Hang tight! Polishing your custom guide...")
            }
        }
        is UiState.Success -> {
            AnimatedVisibility(visible = true, enter = fadeIn(tween(350))) {
                ItineraryResultScreen(
                    result = itineraryState.data,
                    onBack = onReset,
                    onSave = {
                        val saved = onSaveTrip()
                        Toast.makeText(context, if (saved) "Trip saved successfully." else "Trip already saved.", Toast.LENGTH_SHORT).show()
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("TripGenie Itinerary", itineraryState.data.toCopyText())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Itinerary copied to clipboard.", Toast.LENGTH_SHORT).show()
                    },
                    onRouteMap = onShowRouteMap
                )
            }
        }
        else -> {
            // Form
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    if (itineraryState is UiState.Error) {
                        ErrorCard(message = itineraryState.message, onRetry = onGenerate)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                item {
                    SectionCard {
                        SectionHeader("🗺️ Journey Blueprint", "Define your vibe and let our AI craft the perfect route.")
                        TripInput("WHERE TO?", request.destination, onDestinationChange, "Tokyo, Paris, Bali...")
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TripInput(
                                label = "DAYS",
                                value = if (request.durationDays <= 0) "" else request.durationDays.toString(),
                                onValueChange = { v ->
                                    val n = v.filter { it.isDigit() }
                                    onDurationChange(n.toIntOrNull() ?: 0)
                                },
                                placeholder = "e.g. 3",
                                modifier = Modifier.weight(1f)
                            )
                            TripDropdown(
                                label = "BUDGET",
                                selected = request.budget,
                                options = listOf("Budget", "Moderate", "Luxury"),
                                onSelected = onBudgetChange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        TripDropdown("TRAVELER TYPE", request.travelerType,
                            listOf("Solo", "Couple", "Friends", "Family"),
                            onSelected = { onTravelerTypeChange(it) })
                        Spacer(Modifier.height(14.dp))
                        TripInput("INTERESTS", request.interests, onInterestsChange, "Art, Food, Nature...")
                        Spacer(Modifier.height(20.dp))
                        if (request.destination.isNotBlank() && request.durationDays <= 0) {
                            Text("⚠️ Please enter number of days", fontSize = 12.sp,
                                color = WarningOrange, modifier = Modifier.padding(bottom = 6.dp))
                        }
                        PrimaryButton("Generate Itinerary", onGenerate,
                            enabled = request.destination.isNotBlank() && request.durationDays > 0)
                    }
                }
            }
        }
    }
}

@Composable
fun ItineraryResultScreen(
    result: ItineraryResult,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onRouteMap: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title card
        item {
            SectionCard {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(result.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary, lineHeight = 23.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(result.description, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp)
                    }
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }
                Spacer(Modifier.height(14.dp))
                // Action buttons: Save, Copy, Route Map
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Save button
                    ActionButton(
                        icon = null, label = "Save",
                        bgColor = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = onSave
                    )
                    // Copy button
                    ActionButton(
                        icon = null, label = "Copy",
                        bgColor = Color(0xFFE3F2FD), textColor = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f),
                        onClick = onCopy
                    )
                    // Route Map button
                    ActionButton(
                        icon = null, label = "Route Map",
                        bgColor = Color(0xFFFFF3E0), textColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f),
                        onClick = onRouteMap
                    )
                }
            }
        }

        // Day timelines
        items(result.days) { day -> DayTimeline(day = day) }

        // Budget + insights
        item {
            SectionCard {
                Text("📊 Travel Insights", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(14.dp))
                DonutChart(
                    segments = listOf(
                        "Accommodation" to result.budgetBreakdown.accommodation,
                        "Activities"    to result.budgetBreakdown.activities,
                        "Food"          to result.budgetBreakdown.food,
                        "Transport"     to result.budgetBreakdown.transport
                    ),
                    colors = listOf(ChartAccommodation, ChartActivities, ChartFood, ChartTransport)
                )
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(Modifier.height(12.dp))
                Text("🍜 Must-Try Foods", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                result.localFoods.forEach { food ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = AccentTeal, fontWeight = FontWeight.Bold)
                        Text(food, fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("🍽️ Restaurants", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                result.recommendedRestaurants.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = AccentTeal, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(r, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: String? = null, label: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
fun DayTimeline(day: DayPlan) {
    val dayColor = dayColors.getOrElse(day.dayNumber - 1) { PrimaryBlue }
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp),
        CardDefaults.cardColors(containerColor = CardWhite), CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(dayColor).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text("Day ${day.dayNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(day.theme, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
            day.activities.forEachIndexed { index, activity ->
                Row(Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(dayColor))
                        if (index < day.activities.size - 1)
                            Box(Modifier.width(2.dp).height(52.dp).background(dayColor.copy(alpha = 0.25f)))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f).padding(bottom = if (index < day.activities.size - 1) 10.dp else 0.dp)) {
                        Text(activity.time, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dayColor)
                        Text(activity.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(activity.description, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                        if (activity.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Default.LocationOn, null, tint = AccentTeal, modifier = Modifier.size(11.dp))
                                Text(activity.location, fontSize = 11.sp, color = AccentTeal)
                            }
                        }
                        if (activity.estimatedCost.isNotBlank())
                            Text("💰 ${activity.estimatedCost}", fontSize = 11.sp, color = WarningOrange, modifier = Modifier.padding(top = 1.dp))
                    }
                }
            }
        }
    }
}
