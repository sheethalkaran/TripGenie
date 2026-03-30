package com.tripgenie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripgenie.ui.theme.*

@Composable
fun TripGenieNavBar(savedCount: Int = 0, onSavedTripsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(PrimaryButtonGradient),
                contentAlignment = Alignment.Center
            ) { Text("T", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
            Spacer(Modifier.width(8.dp))
            Text("TripGenie", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = TextPrimary)
        }
        // Clean "Saved Trips" button - no emoji
        Box(
            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(PrimaryBlue)
                .clickable { onSavedTripsClick() }.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                if (savedCount > 0) "Saved Trips  $savedCount" else "Saved Trips",
                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
            )
        }
    }
}
