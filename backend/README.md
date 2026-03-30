# TripGenie Backend — FastAPI

TripGenie backend is a FastAPI server that powers all AI features for the TripGenie app. It handles all communication with the Google Gemini API and exposes REST endpoints for itinerary, packing, food, and chat features.

## Local Development Setup

1. **Install Prerequisites**
   - Python 3.10+
   - pip (Python package manager)
2. **Clone the Repository**
   - `git clone <repo-url>`
3. **Install Dependencies**
   - `cd backend`
   - `pip install -r requirements.txt`
4. **Configure Environment Variables**
   - Copy `.env.example` to `.env`
   - Add your actual Gemini API key in `.env` (`GEMINI_API_KEY=...`)
5. **Run the Server**
   - `uvicorn main:app --reload --port 8000`
6. **Test the API**
   - Open http://localhost:8000/docs for Swagger UI

## Project Structure

```
backend/
├── main.py              # FastAPI app, endpoints, Gemini integration
├── requirements.txt     # Python dependencies
├── .env.example        # Example environment config
├── render.yaml          # Render.com deployment config
├── __pycache__/        # Python bytecode cache (ignored)
└── ...
```

## Architecture Overview

- **Framework:** FastAPI (async Python web framework)
- **AI Integration:** Google Gemini API (via `google-generativeai`)
- **Config:** Environment variables loaded from `.env` (never commit secrets)
- **Endpoints:** Each feature (itinerary, packing, food, chat) has a dedicated POST endpoint
- **Models:** Pydantic models define request/response schemas for validation and docs
- **CORS:** Enabled for all origins (for local/mobile frontend access)
- **Error Handling:** Returns clear HTTP errors for invalid input or Gemini failures
- **Deployment:** Ready for Render.com (see `render.yaml`)

### Endpoint Flow
1. Client sends POST request to `/itinerary`, `/packing`, `/food`, or `/chat`
2. FastAPI validates input using Pydantic models
3. Backend builds a prompt and calls Gemini API (with fallback to multiple models)
4. Parses and cleans Gemini's response (ensures valid JSON)
5. Returns structured JSON to the client

## Key Files
- `main.py` — All endpoints, Gemini integration, request/response models
- `requirements.txt` — FastAPI, Gemini SDK, dotenv, Pydantic
- `.env.example` — Template for environment variables
- `render.yaml` — Render.com deployment config

## Deployment (Render.com)
1. Push backend folder to a GitHub repository
2. Create a new Web Service on Render.com
3. Set build/start commands as in `render.yaml`
4. Add `GEMINI_API_KEY` as an environment variable in Render dashboard
5. Deploy and get your public API URL

## API Endpoints

| Method | Path         | Description                    |
|--------|--------------|--------------------------------|
| GET    | `/`          | Health check                   |
| GET    | `/health`    | Health check                   |
| POST   | `/itinerary` | Generate trip itinerary        |
| POST   | `/packing`   | Generate packing list          |
| POST   | `/food`      | Get local food recommendations |
| POST   | `/chat`      | Chat with travel assistant     |

## Environment Variables
- `GEMINI_API_KEY` — Your Google Gemini API key (required, never commit)

## Troubleshooting
- If you see `GEMINI_API_KEY environment variable not set`, check your `.env` file and environment
- For CORS errors, ensure backend is running and accessible from your frontend
- For Gemini quota or API errors, check your API key and usage limits

---

For frontend setup and usage, see `../frontend/README.md`.
