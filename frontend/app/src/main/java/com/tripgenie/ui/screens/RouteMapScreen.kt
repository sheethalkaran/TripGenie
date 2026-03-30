package com.tripgenie.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.tripgenie.data.models.ItineraryResult
import com.tripgenie.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RouteMapScreen(result: ItineraryResult, onBack: () -> Unit) {
    var selectedDay by remember { mutableIntStateOf(-1) }
    val dayColorsHex = listOf("#2D4DE0","#1EB8A6","#F97316","#8B5CF6","#EC4899","#14B8A6")

    // Build JSON data for selected day
    val mapData = remember(selectedDay) { buildMapData(result, selectedDay, dayColorsHex) }

    // Keep WebView alive across recompositions
    var webViewReady by remember { mutableStateOf(false) }
    val webViewHolder = remember { mutableStateOf<WebView?>(null) }

    // When data or webview becomes ready, inject the data
    LaunchedEffect(mapData, webViewReady) {
        if (webViewReady) {
            val escaped = mapData.replace("\\", "\\\\").replace("'", "\\'")
            webViewHolder.value?.evaluateJavascript("initMap('$escaped')", null)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F4FF)).statusBarsPadding()) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
            }
            Text("Route Map", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        // Day filter
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { DayPill("All Days", selectedDay == -1, PrimaryBlue) { selectedDay = -1 } }
            items(result.days) { day ->
                val col = try {
                    Color(android.graphics.Color.parseColor(dayColorsHex.getOrElse(day.dayNumber - 1) { "#2D4DE0" }))
                } catch (e: Exception) { PrimaryBlue }
                DayPill("Day ${day.dayNumber}", selectedDay == day.dayNumber, col) {
                    selectedDay = day.dayNumber
                }
            }
        }

        // WebView loads from asset - works 100% offline, no CDN needed
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = false
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        blockNetworkLoads = false
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        allowFileAccess = true
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = true
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = true
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            // Page loaded - now inject map data
                            webViewReady = true
                            webViewHolder.value = view
                            val escaped = mapData.replace("\\", "\\\\").replace("'", "\\'")
                            view.evaluateJavascript("initMap('$escaped')", null)
                        }
                    }

                    webViewHolder.value = this
                    // Load from assets - works completely offline
                    loadUrl("file:///android_asset/map.html")
                }
            },
            update = { wv ->
                webViewHolder.value = wv
                if (webViewReady) {
                    val escaped = mapData.replace("\\", "\\\\").replace("'", "\\'")
                    wv.evaluateJavascript("initMap('$escaped')", null)
                }
            }
        )
    }
}

@Composable
fun DayPill(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (selected) color else Color(0xFFEEEEEE))
            .clickable { onClick() }.padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(label, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary)
    }
}

// Build JSON payload for the map - passed to initMap() in map.html
fun buildMapData(result: ItineraryResult, selectedDay: Int, dayColorsHex: List<String>): String {
    val days = if (selectedDay == -1) result.days else result.days.filter { it.dayNumber == selectedDay }
    val validPts = days.flatMap { d ->
        d.activities.filter { it.lat != 0.0 && it.lng != 0.0 && it.lat.isFinite() && it.lng.isFinite() }
    }

    val markersArr = JSONArray()
    val routesArr  = JSONArray()

    days.forEach { day ->
        val col = dayColorsHex.getOrElse(day.dayNumber - 1) { "#2D4DE0" }
        val valid = day.activities.filter {
            it.lat != 0.0 && it.lng != 0.0 && it.lat.isFinite() && it.lng.isFinite()
        }
        valid.forEach { a ->
            val m = JSONObject()
            m.put("lat", a.lat)
            m.put("lng", a.lng)
            m.put("col", col)
            val popup = "<b>${a.title.take(50)}</b><br><small>${a.time}</small><br><span style='color:#1EB8A6'>${a.location.take(50)}</span>"
            m.put("popup", popup)
            markersArr.put(m)
        }
        if (valid.size >= 2) {
            val ptsArr = JSONArray()
            valid.forEach { a -> ptsArr.put(JSONArray().put(a.lat).put(a.lng)) }
            val r = JSONObject()
            r.put("pts", ptsArr)
            r.put("col", col)
            routesArr.put(r)
        }
    }

    val center = if (validPts.isNotEmpty()) {
        val lat = validPts.map { it.lat }.average()
        val lng = validPts.map { it.lng }.average()
        JSONArray().put(lat).put(lng)
    } else {
        JSONArray().put(13.3409).put(74.7421) // Default: Udupi
    }

    val data = JSONObject()
    data.put("markers", markersArr)
    data.put("routes", routesArr)
    data.put("center", center)
    data.put("zoom", 13)
    return data.toString()
}
