package com.tripgenie.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.tripgenie.data.models.*
import com.tripgenie.ui.components.*
import com.tripgenie.ui.theme.*

@Composable
fun FoodScreen(
    foodState: UiState<List<FoodItem>>,
    foodLoadMoreState: UiState<Unit>,
    foodItems: List<FoodItem>,
    region: String,
    onRegionChange: (String) -> Unit,
    onFetch: () -> Unit,
    onLoadMore: () -> Unit,
    onReset: () -> Unit = {}
) {
    if (foodItems.isNotEmpty() && foodState is UiState.Success) {
        FoodResultScreen(foodItems, foodLoadMoreState, region, onLoadMore, onReset)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp)
    ) {
        item {
            if (foodState is UiState.Error) {
                ErrorCard(message = foodState.message, onRetry = onFetch)
                Spacer(Modifier.height(12.dp))
            }
        }
        item {
            SectionCard {
                SectionHeader("Taste Scout", "Discover authentic flavors based on local culinary data.")
                TripInput("REGION OR CITY", region, onRegionChange, "Udupi, Mysore, Mumbai...")
                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = "Fetch Culinary Insights",
                    onClick = onFetch,
                    enabled = region.isNotBlank(),
                    isLoading = foodState is UiState.Loading
                )
            }
        }
    }
}

@Composable
fun FoodResultScreen(
    foodItems: List<FoodItem>,
    foodLoadMoreState: UiState<Unit>,
    region: String,
    onLoadMore: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 100.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Local Specialties \u2014 ${region.trim().replaceFirstChar { it.uppercase() }}",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onReset, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
        val rows = foodItems.chunked(2)
        items(rows) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { food -> FoodCard(food = food, modifier = Modifier.weight(1f)) }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF3E0))
                    .border(1.5.dp, WarningOrange, RoundedCornerShape(14.dp))
                    .clickable(enabled = foodLoadMoreState !is UiState.Loading) { onLoadMore() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                if (foodLoadMoreState is UiState.Loading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = WarningOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Finding more dishes...", color = WarningOrange, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                } else {
                    Text("Load Extra Foods", color = WarningOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Food Card — matches screenshot exactly ──────────────────────────────
// White card, emoji directly on white (no box), badge, name, description, location
@Composable
fun FoodCard(food: FoodItem, modifier: Modifier = Modifier) {
    // Use AI-provided emoji; fallback by food name keywords if blank/generic
    val emoji = if (food.icon.isNotBlank() && food.icon.trim() != "🍽️")
        food.icon.trim()
    else
        foodEmojiByName(food.name)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 12.dp)
        ) {
            // Emoji directly on white background — no box, no border, no background
            Text(text = emoji, fontSize = 38.sp)

            Spacer(Modifier.height(8.dp))

            // Light blue pill badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE3EEFF))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = food.restaurant.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))

            // Food name
            Text(
                text = food.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                maxLines = 2,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(4.dp))

            // Description
            Text(
                text = food.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, null,
                    tint = AccentTeal, modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = food.bestSpot,
                    fontSize = 11.sp, color = AccentTeal,
                    fontWeight = FontWeight.Medium, maxLines = 2,
                    lineHeight = 15.sp, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Keyword-based emoji fallback — only used when AI returns blank/generic icon
fun foodEmojiByName(name: String): String {
    val n = name.lowercase()
    return when {
        n.contains("dosa") || n.contains("crepe") -> "🥞"
        n.contains("idli") || n.contains("kadubu") || n.contains("sanna") -> "🫓"
        n.contains("rice") || n.contains("biryani") || n.contains("pulao") -> "🍚"
        n.contains("curry") || n.contains("masala") || n.contains("gravy") -> "🍛"
        n.contains("noodle") || n.contains("pasta") -> "🍜"
        n.contains("bread") || n.contains("roti") || n.contains("naan") || n.contains("bun") -> "🍞"
        n.contains("fish") || n.contains("gassi") || n.contains("fry") && n.contains("fish") -> "🐟"
        n.contains("chicken") || n.contains("sukka") || n.contains("kori") -> "🍗"
        n.contains("prawn") || n.contains("shrimp") || n.contains("seafood") -> "🦐"
        n.contains("ice cream") || n.contains("gadbad") || n.contains("sundae") -> "🍨"
        n.contains("sweet") || n.contains("halwa") || n.contains("laddu") -> "🍬"
        n.contains("cake") || n.contains("dessert") -> "🎂"
        n.contains("soup") || n.contains("rasam") || n.contains("sambar") -> "🍲"
        n.contains("vada") || n.contains("baje") || n.contains("bajji") || n.contains("fritter") -> "🧆"
        n.contains("salad") || n.contains("vegetable") -> "🥗"
        n.contains("egg") || n.contains("omelette") -> "🥚"
        n.contains("pizza") -> "🍕"
        n.contains("burger") || n.contains("sandwich") -> "🍔"
        n.contains("tea") || n.contains("chai") || n.contains("coffee") -> "☕"
        n.contains("juice") || n.contains("lassi") || n.contains("drink") -> "🥤"
        n.contains("patrode") || n.contains("colocasia") -> "🌿"
        n.contains("pundi") || n.contains("dumpling") || n.contains("momo") -> "🥟"
        n.contains("appam") || n.contains("kallappam") -> "🫓"
        n.contains("rotti") || n.contains("roti") -> "🫓"
        else -> "🍽️"
    }
}

// Keep getFoodEmoji as alias for backward compatibility
fun getFoodEmoji(icon: String, name: String): String =
    if (icon.isNotBlank() && icon.trim() != "🍽️") icon.trim() else foodEmojiByName(name)
