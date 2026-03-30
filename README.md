# TripGenie

TripGenie is an AI-powered travel planning application that helps users generate personalized trip itineraries, packing lists, local food recommendations, and provides a travel assistant chatbot. The project consists of a FastAPI backend and a modern Android frontend built with Kotlin and Jetpack Compose.

## Features
- Generate multi-day travel itineraries with real activities, locations, and budget breakdowns
- Create weather and activity-specific packing lists
- Discover authentic local foods and restaurants for any region
- Chatbot assistant for travel tips, logistics, and local insights

## Tech Stack
- **Backend:** Python, FastAPI, Google Gemini API, Pydantic
- **Frontend:** Kotlin, Jetpack Compose, MVVM architecture, Coroutines, OkHttp

## Architecture
- The backend exposes REST API endpoints for itinerary, packing, food, and chat features. All AI generation is handled server-side using Gemini models.
- The frontend uses MVVM architecture. All API calls are made via a repository class to the backend. No API keys are stored in the app.

## How It Works
1. User enters trip details in the Android app
2. App sends requests to the backend (deployed on Render or locally)
3. Backend generates responses using Gemini and returns structured JSON
4. App displays results in a modern, responsive UI

## Project Structure
- `backend/` — FastAPI server, Gemini integration, REST endpoints
- `frontend/` — Android app, MVVM, Compose UI, repository pattern

## Setup
- See `backend/README.md` for backend setup and deployment
- See `frontend/README.md` for Android app setup and usage

---

For more details, refer to the respective README files in each folder.