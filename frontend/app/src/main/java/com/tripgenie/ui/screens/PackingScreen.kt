package com.tripgenie.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.tripgenie.data.models.*
import com.tripgenie.ui.components.*
import com.tripgenie.ui.theme.*

@Composable
fun PackingScreen(
    packingState: UiState<PackingResult>,
    request: PackingRequest,
    checkedItems: List<String>,
    onDestinationChange: (String) -> Unit,
    onWeatherChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onActivitiesChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onToggleItem: (String) -> Unit,
    getTotalItems: (PackingResult) -> Int,
    getPackedCount: () -> Int,
    onReset: () -> Unit = {}
) {
    // Hide form and show only results when Success
    if (packingState is UiState.Success) {
        PackingResultScreen(
            result = packingState.data,
            checkedItems = checkedItems,
            onToggleItem = onToggleItem,
            getTotalItems = getTotalItems,
            getPackedCount = getPackedCount,
            onReset = onReset
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Form card
        item {
            SectionCard {
                SectionHeader("🎒 Smart Checklist", "Never forget an essential. Tailored for your destination.")
                TripInput("DESTINATION", request.destination, onDestinationChange, "Manali, Goa, Coorg...")
                Spacer(Modifier.height(14.dp))
                TripDropdown("WEATHER TYPE", request.weatherType,
                    listOf("Hot / Summer", "Cold / Winter", "Rainy", "Mixed"), onWeatherChange)
                Spacer(Modifier.height(14.dp))
                TripInput(
                    label = "TRIP DURATION (DAYS)",
                    value = if (request.tripDuration <= 0) "" else request.tripDuration.toString(),
                    onValueChange = { v ->
                        val n = v.filter { it.isDigit() }
                        onDurationChange(n.toIntOrNull() ?: 0)
                    },
                    placeholder = "e.g. 5"
                )
                Spacer(Modifier.height(14.dp))
                TripInput("MAIN ACTIVITIES", request.mainActivities, onActivitiesChange, "Hiking, Work, Parties...")
                Spacer(Modifier.height(20.dp))
                PrimaryButton(
                    text = "Generate Packing List",
                    onClick = onGenerate,
                    enabled = request.destination.isNotBlank(),
                    isLoading = packingState is UiState.Loading
                )
            }
        }

        // Error
        if (packingState is UiState.Error) {
            item {
                ErrorCard(message = packingState.message, onRetry = onGenerate)
            }
        }

        // Results
        if (packingState is UiState.Success) {
            val result = packingState.data
            val total  = getTotalItems(result)
            val packed = getPackedCount()
            val progress = if (total > 0) packed.toFloat() / total else 0f

            // Progress card
            item {
                SectionCard {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Column {
                            Text("Packing Readiness", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("$packed of $total items packed", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            "${(progress * 100).toInt()}%",
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (progress == 1f) SuccessGreen else PrimaryBlue
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (progress == 1f) SuccessGreen else PrimaryBlue,
                        trackColor = BorderLight
                    )
                    if (progress == 1f) {
                        Spacer(Modifier.height(10.dp))
                        Text("🎉 All packed! Ready to go!", color = SuccessGreen, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Category cards
            items(result.categories) { category ->
                PackingCategoryCard(category = category, checkedItems = checkedItems, onToggleItem = onToggleItem)
            }
        }
    }
}

@Composable
fun PackingCategoryCard(category: PackingCategory, checkedItems: List<String>, onToggleItem: (String) -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    val checkedCount = category.items.count { checkedItems.contains("${category.name}:${it.name}") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.icon, fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(category.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(
                            "$checkedCount/${category.items.size} packed",
                            fontSize = 11.sp,
                            color = if (checkedCount == category.items.size) SuccessGreen else TextSecondary
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null, tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(bottom = 10.dp))
                    category.items.forEach { item ->
                        val key = "${category.name}:${item.name}"
                        val isChecked = checkedItems.contains(key)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onToggleItem(key) }.padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked, onCheckedChange = { onToggleItem(key) },
                                colors = CheckboxDefaults.colors(checkedColor = SuccessGreen, uncheckedColor = BorderLight),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                item.name, fontSize = 13.sp,
                                color = if (isChecked) TextSecondary else TextPrimary,
                                style = if (isChecked) LocalTextStyle.current.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ) else LocalTextStyle.current
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PackingPreview() {
    TripGenieTheme {
        GradientBackground {
            PackingScreen(
                packingState = UiState.Idle,
                request = PackingRequest("Manali", "Cold / Winter", 5, "Hiking"),
                checkedItems = emptyList(), onDestinationChange = {}, onWeatherChange = {},
                onDurationChange = {}, onActivitiesChange = {}, onGenerate = {},
                onToggleItem = {}, getTotalItems = { 0 }, getPackedCount = { 0 }
            )
        }
    }
}

@Composable
fun PackingResultScreen(
    result: PackingResult,
    checkedItems: List<String>,
    onToggleItem: (String) -> Unit,
    getTotalItems: (PackingResult) -> Int,
    getPackedCount: () -> Int,
    onReset: () -> Unit
) {
    val total = getTotalItems(result)
    val packed = getPackedCount()
    val progress = if (total > 0) packed.toFloat() / total else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionCard {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Packing Readiness", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$packed of $total items packed", fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${(progress * 100).toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (progress == 1f) SuccessGreen else PrimaryBlue)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (progress == 1f) SuccessGreen else PrimaryBlue,
                    trackColor = BorderLight
                )
                if (progress == 1f) {
                    Spacer(Modifier.height(8.dp))
                    Text("🎉 All packed! Ready to go!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
        items(result.categories) { category ->
            PackingCategoryCard(category, checkedItems, onToggleItem)
        }
    }
}
