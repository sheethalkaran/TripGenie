package com.tripgenie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.tripgenie.data.models.SavedTrip
import com.tripgenie.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedTripsScreen(
    savedTrips: List<SavedTrip>,
    onViewTrip: (SavedTrip) -> Unit,
    onDeleteTrip: (String) -> Unit,
    onClose: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text("Saved Trips", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            Text("${savedTrips.size} trips", fontSize = 12.sp, color = TextSecondary)
        }

        if (savedTrips.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No trips yet", fontSize = 16.sp, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    Text("No saved trips", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Generate an itinerary and save it!", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedTrips.reversed()) { trip ->
                    val result = runCatching { trip.toResult() }.getOrNull()
                    SavedTripCard(
                        trip = trip,
                        title = result?.title ?: "Saved Trip",
                        description = result?.description ?: "",
                        dayCount = result?.days?.size ?: 0,
                        savedAt = dateFormat.format(Date(trip.savedAt)),
                        onView = { onViewTrip(trip) },
                        onDelete = { onDeleteTrip(trip.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedTripCard(
    trip: SavedTrip,
    title: String,
    description: String,
    dayCount: Int,
    savedAt: String,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip?") },
            text = { Text("This will permanently delete \"$title\". This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, lineHeight = 21.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(description, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp, maxLines = 2)
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFEBF4FF)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$dayCount days", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                Text("Saved $savedAt", fontSize = 11.sp, color = TextHint)
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryButtonGradient)
                    .clickable { onView() }
                    .padding(vertical = 10.dp),
                Arrangement.Center,
                Alignment.CenterVertically
            ) {
                Text("View Itinerary", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}
