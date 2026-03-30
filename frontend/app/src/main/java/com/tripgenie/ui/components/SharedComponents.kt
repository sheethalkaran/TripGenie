package com.tripgenie.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.tripgenie.ui.theme.*

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(AppBackgroundGradient)) { content() }
}

@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, isLoading: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp))
            .background(if (enabled) PrimaryButtonGradient else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripInput(
    label: String, value: String, onValueChange: (String) -> Unit,
    placeholder: String = "", modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextHint, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(11.dp),
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderLight,
                focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF9FAFB),
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDropdown(
    label: String, selected: String, options: List<String>,
    onSelected: (String) -> Unit, modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selected, onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        null, tint = TextSecondary)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(11.dp),
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderLight,
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt, fontSize = 13.sp) },
                        onClick = { onSelected(opt); expanded = false })
                }
            }
        }
    }
}

@Composable
fun SegmentedControl(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(Color(0xFFE5E7EB)).padding(3.dp)) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(11.dp))
                .background(if (selected) Color.White else Color.Transparent)
                .shadow(if (selected) 2.dp else 0.dp, RoundedCornerShape(11.dp))
                .clickable { onTabSelected(index) }.padding(vertical = 9.dp),
                contentAlignment = Alignment.Center) {
                Text(tab, fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) PrimaryBlue else TextSecondary)
            }
        }
    }
}

// Fix: proper blur/dim loading modal (not grey background)
@Composable
fun LoadingModal(message: String = "Generating your plan...") {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp).widthIn(max = 320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "plane")
                val offset by infiniteTransition.animateFloat(
                    initialValue = -8f, targetValue = 8f,
                    animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
                    label = "y"
                )
                Text("✈️", fontSize = 48.sp, modifier = Modifier.offset(y = offset.dp))
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(16.dp))
                Text(message, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun DonutChart(segments: List<Pair<String, Int>>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = segments.sumOf { it.second }.toFloat()
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(150.dp)) {
                var startAngle = -90f
                segments.forEachIndexed { i, (_, value) ->
                    val sweep = (value / total) * 360f
                    drawArc(color = colors.getOrElse(i) { PrimaryBlue }, startAngle = startAngle,
                        sweepAngle = sweep - 2f, useCenter = false,
                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round))
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Budget", fontSize = 10.sp, color = TextSecondary)
                Text("Split", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        segments.forEachIndexed { i, (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(colors.getOrElse(i) { PrimaryBlue }))
                    Spacer(Modifier.width(7.dp))
                    Text(label, fontSize = 11.sp, color = TextSecondary)
                }
                Text("$value%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp),
        CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 30.sp)
            Spacer(Modifier.height(6.dp))
            Text("Something went wrong", fontWeight = FontWeight.Bold, color = ErrorRed, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(message, color = TextSecondary, fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(14.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                Text("Try Again", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun RestaurantBadge(name: String) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(BadgeBg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(name.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BadgeText,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
