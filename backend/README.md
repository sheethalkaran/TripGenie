# TripGenie Backend — FastAPI

## Local Development

```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Create your .env file
cp .env.example .env
# Edit .env and add your real GEMINI_API_KEY

# 3. Run the server
uvicorn main:app --reload --port 8000
```

Open http://localhost:8000/docs for the interactive Swagger UI.

## Deploy to Render (Free)

1. Push this folder to a GitHub repository
2. Go to https://render.com and sign in
3. Click **New → Web Service**
4. Connect your GitHub repo
5. Set these settings:
   - **Build Command:** `pip install -r requirements.txt`
   - **Start Command:** `uvicorn main:app --host 0.0.0.0 --port $PORT`
6. Under **Environment Variables**, add:
   - Key: `GEMINI_API_KEY`  Value: your actual Gemini API key
7. Click **Deploy**

Your backend URL will be: `https://tripgenie-backend.onrender.com`

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Health check |
| GET | `/health` | Health check |
| POST | `/itinerary` | Generate trip itinerary |
| POST | `/packing` | Generate packing list |
| POST | `/food` | Get local food recommendations |
| POST | `/chat` | Chat with travel assistant |

## After Deployment

Update `BASE_URL` in the Android app's `GeminiRepository.kt`:
```kotlin
private val BASE_URL = "https://your-actual-render-url.onrender.com"
```
