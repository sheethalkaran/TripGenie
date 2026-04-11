package com.tripgenie.data.repository

import com.tripgenie.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class GeminiRepository {

    // ── CHANGE THIS to your Render URL after deploying ───────────────────────
    // Example: "https://tripgenie-backend.onrender.com"
    private val BASE_URL = "https://tripgenie-backend-1nb5.onrender.com"
    // ────────────────────────────────────────────────────────────────────────

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    // ── Session-level in-memory cache (lives as long as the app process) ─────
    private val sessionCache = HashMap<String, String>()

    private fun cacheKey(vararg parts: String): String {
        val raw = parts.joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    // ── Core HTTP helper ─────────────────────────────────────────────────────

    private suspend fun post(path: String, bodyJson: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .post(bodyJson.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val detail = runCatching {
                JSONObject(responseBody).optString("detail", responseBody.take(200))
            }.getOrElse { responseBody.take(200) }
            throw Exception("Backend error ${response.code}: $detail")
        }

        responseBody
    }

    private fun cleanJson(raw: String): String {
        val start = raw.indexOfFirst { it == '{' || it == '[' }
        val end = raw.indexOfLast { it == '}' || it == ']' }
        return if (start != -1 && end != -1 && end > start) raw.substring(start, end + 1) else raw.trim()
    }

    // ── Itinerary ────────────────────────────────────────────────────────────

    suspend fun generateItinerary(request: ItineraryRequest): ItineraryResult {
        val key = cacheKey(
            "itinerary",
            request.destination,
            request.durationDays.toString(),
            request.budget,
            request.travelerType,
            request.interests
        )
        sessionCache[key]?.let { return parseItinerary(it, request.destination) }

        val bodyJson = JSONObject().apply {
            put("destination", request.destination)
            put("durationDays", request.durationDays)
            put("budget", request.budget)
            put("travelerType", request.travelerType)
            put("interests", request.interests)
            put("useGrounding", request.useGrounding)
        }.toString()

        val response = post("/itinerary", bodyJson)
        val raw = JSONObject(response).getString("result")
        val cleaned = cleanJson(raw)
        sessionCache[key] = cleaned
        return parseItinerary(cleaned, request.destination)
    }

    private fun parseItinerary(text: String, destination: String): ItineraryResult {
        val json = JSONObject(text)
        val daysArr = json.getJSONArray("days")
        val days = (0 until daysArr.length()).map { i ->
            val d = daysArr.getJSONObject(i)
            val actsArr = d.getJSONArray("activities")
            DayPlan(
                dayNumber = d.optInt("dayNumber", i + 1),
                theme = d.optString("theme", "Day ${i + 1}"),
                activities = (0 until actsArr.length()).map { j ->
                    val a = actsArr.getJSONObject(j)
                    Activity(
                        a.optString("time"), a.optString("title"), a.optString("description"),
                        a.optString("location"), a.optString("estimatedCost"),
                        a.optDouble("lat", 0.0), a.optDouble("lng", 0.0)
                    )
                }
            )
        }
        val b = json.optJSONObject("budgetBreakdown") ?: JSONObject()
        val lf = json.optJSONArray("localFoods") ?: JSONArray()
        val rr = json.optJSONArray("recommendedRestaurants") ?: JSONArray()
        return ItineraryResult(
            title = json.optString("title", "Your Trip to $destination"),
            description = json.optString("description", ""),
            days = days,
            budgetBreakdown = BudgetBreakdown(
                b.optInt("accommodation", 40), b.optInt("activities", 20),
                b.optInt("food", 25), b.optInt("transport", 15)
            ),
            localFoods = (0 until lf.length()).map { lf.getString(it) },
            recommendedRestaurants = (0 until rr.length()).map { rr.getString(it) }
        )
    }

    // ── Packing ──────────────────────────────────────────────────────────────

    suspend fun generatePackingList(request: PackingRequest): PackingResult {
        val key = cacheKey(
            "packing",
            request.destination,
            request.weatherType,
            request.tripDuration.toString(),
            request.mainActivities
        )
        sessionCache[key]?.let { cached ->
            val arr = JSONArray(cached)
            return PackingResult(categories = (0 until arr.length()).map { i ->
                val c = arr.getJSONObject(i)
                val ia = c.getJSONArray("items")
                PackingCategory(
                    c.optString("name", "Category"), c.optString("icon", "📦"),
                    (0 until ia.length()).map { PackingItem(ia.getJSONObject(it).optString("name", "Item")) }
                )
            })
        }

        val bodyJson = JSONObject().apply {
            put("destination", request.destination)
            put("weatherType", request.weatherType)
            put("tripDuration", request.tripDuration)
            put("mainActivities", request.mainActivities)
        }.toString()

        val response = post("/packing", bodyJson)
        val raw = JSONObject(response).getString("result")
        val cleaned = cleanJson(raw)
        sessionCache[key] = cleaned

        val arr = JSONArray(cleaned)
        return PackingResult(categories = (0 until arr.length()).map { i ->
            val c = arr.getJSONObject(i)
            val ia = c.getJSONArray("items")
            PackingCategory(
                c.optString("name", "Category"), c.optString("icon", "📦"),
                (0 until ia.length()).map { PackingItem(ia.getJSONObject(it).optString("name", "Item")) }
            )
        })
    }

    // ── Food ─────────────────────────────────────────────────────────────────

    suspend fun generateFoodInsights(region: String, alreadyShown: List<String> = emptyList()): List<FoodItem> {
        // Only cache the first-load (no alreadyShown) — "load more" should always be fresh
        val cacheEnabled = true
        val key = cacheKey("food", region, alreadyShown.sorted().joinToString(","))

        if (cacheEnabled) {
            sessionCache[key]?.let { cached ->
                val arr = JSONArray(cached)
                return (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    FoodItem(
                        o.optString("name", "Local Dish"),
                        o.optString("restaurant", "Local Restaurant"),
                        o.optString("description", "A delicious local specialty."),
                        o.optString("bestSpot", "Local area"),
                        o.optString("icon", "🍽️")
                    )
                }
            }
        }

        val bodyJson = JSONObject().apply {
            put("region", region)
            put("alreadyShown", JSONArray(alreadyShown))
        }.toString()

        val response = post("/food", bodyJson)
        val raw = JSONObject(response).getString("result")
        val cleaned = cleanJson(raw)

        if (cacheEnabled) sessionCache[key] = cleaned

        val arr = JSONArray(cleaned)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            FoodItem(
                o.optString("name", "Local Dish"),
                o.optString("restaurant", "Local Restaurant"),
                o.optString("description", "A delicious local specialty."),
                o.optString("bestSpot", "Local area"),
                o.optString("icon", "🍽️")
            )
        }
    }

    // ── Chat ─────────────────────────────────────────────────────────────────

    suspend fun chat(userMessage: String, history: List<ChatMessage>): String {
        val historyArr = JSONArray()
        history.takeLast(4).forEach { msg ->
            historyArr.put(JSONObject().apply {
                put("content", msg.content)
                put("isUser", msg.isUser)
            })
        }
        val bodyJson = JSONObject().apply {
            put("message", userMessage)
            put("history", historyArr)
        }.toString()

        val response = post("/chat", bodyJson)
        return JSONObject(response).getString("reply")
    }

    suspend fun pingServer() = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("$BASE_URL/health")
                .get()
                .build()
            client.newCall(request).execute().close()
        }
    }
}
