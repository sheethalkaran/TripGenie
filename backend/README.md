# TripGenie — FastAPI Backend

Python backend that powers all AI features for TripGenie. It handles all communication with the Google Gemini API and exposes REST endpoints for itinerary, packing, food, and chat features.

## Local Development Setup

1. **Install Prerequisites**
   - Python 3.10+
   - pip (Python package manager)

2. **Clone the Repository**
   ```bash
   git clone https://github.com/sheethalkaran/TripGenie.git
   cd TripGenie/backend
   ```

3. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure Environment Variables**
   - Copy `.env.example` to `.env`
   - Add your Gemini API key in `.env`: `GEMINI_API_KEY=<your-key>`

5. **Run the Server**
   ```bash
   uvicorn main:app --reload --port 8000
   ```

6. **Test the API**
   - Open http://localhost:8000/docs for Swagger UI

## Project Structure

```
backend/
├── main.py              # FastAPI app with endpoints, Gemini integration, Pydantic models
├── requirements.txt     # Python dependencies
├── .env.example         # Environment variable template
├── render.yaml          # Render.com deployment configuration
└── .gitignore           # Excludes .env, __pycache__, venv
```

## Architecture

```
Client (Android App)
        |  POST /itinerary, /packing, /food, /chat
        v
FastAPI (main.py)
        |  Pydantic validates request body
        |  Builds prompt string
        v
call_gemini(prompt)
        |  Tries models in order: gemini-2.5-flash-lite → gemini-2.5-flash
        |                          → gemini-2.0-flash → gemini-1.5-flash
        |  Retries next model on quota errors
        v
Google Gemini API
        |  Returns raw text
        v
clean_json(raw)
        |  Strips markdown fences, finds first { or [
        v
JSON validation
        |  Raises 500 if unparseable
        v
{ "result": cleaned_json_string }
```

## Key Files

- **main.py** — All endpoints, Gemini integration, request/response models
- **requirements.txt** — FastAPI, Gemini SDK, dotenv, Pydantic
- **.env.example** — Template for environment variables
- **render.yaml** — Render.com deployment config

## API Endpoints

| Method | Path         | Description                    |
|--------|--------------|--------------------------------|
| GET    | `/`          | Health check                   |
| GET    | `/health`    | Health check                   |
| POST   | `/itinerary` | Generate trip itinerary        |
| POST   | `/packing`   | Generate categorised packing list          |
| POST   | `/food`      | Get local food and restaurant recommendations |
| POST   | `/chat`      | Chat with the Concierge Genie travel assistant     |

## Deployment (Render.com)

1. Push backend folder to a GitHub repository.
2. Create a new Web Service on Render.com.
3. Set build/start commands as specified in `render.yaml`.
4. Add `GEMINI_API_KEY` as an environment variable in the Render dashboard.
5. Deploy and retrieve your public API URL.

## Troubleshooting

| Issue                 | Fix                                                                      |
|------------------------|--------------------------------------------------------------------------|
| `GEMINI_API_KEY not set` | Ensure `.env` file exists with the correct API key                      |
| 503 from all models   | Gemini quota exceeded. Wait and retry, or check API key limits           |
| CORS error from Android | Ensure `BASE_URL` in the Android app has no trailing slash               |
| Port already in use   | Change port: `uvicorn main:app --reload --port 8001`                    |

For Android app setup, see [frontend/README.md](../frontend/README.md).
