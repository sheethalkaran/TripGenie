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
import java.util.concurrent.TimeUnit

class GeminiRepository {

    // ── CHANGE THIS to your Render URL after deploying ───────────────────────
    // Example: "https://tripgenie-backend.onrender.com"
    private val BASE_URL = "https://tripgenie-backend.onrender.com"
    // ────────────────────────────────────────────────────────────────────────

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

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
        return parseItinerary(cleanJson(raw), request.destination)
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
        val bodyJson = JSONObject().apply {
            put("destination", request.destination)
            put("weatherType", request.weatherType)
            put("tripDuration", request.tripDuration)
            put("mainActivities", request.mainActivities)
        }.toString()

        val response = post("/packing", bodyJson)
        val raw = JSONObject(response).getString("result")
        val arr = JSONArray(cleanJson(raw))
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
        val bodyJson = JSONObject().apply {
            put("region", region)
            put("alreadyShown", JSONArray(alreadyShown))
        }.toString()

        val response = post("/food", bodyJson)
        val raw = JSONObject(response).getString("result")
        val arr = JSONArray(cleanJson(raw))
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
}
