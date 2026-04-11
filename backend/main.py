import os
import json
import asyncio
import hashlib
import time as _time
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import google.generativeai as genai
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
if not GEMINI_API_KEY:
    raise RuntimeError("GEMINI_API_KEY environment variable not set")

genai.configure(api_key=GEMINI_API_KEY)

app = FastAPI(title="TripGenie API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Model list (only real, working models) ───────────────────────────────────
MODELS = [
    "gemini-3-flash-preview",
    "gemini-2.0-flash-lite",
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-2.5-flash",
    "gemini-1.5-flash-8b",
]

# ── In-memory cache (survives for process lifetime, TTL = 24 hours) ──────────
_cache: dict[str, tuple[str, float]] = {}
CACHE_TTL = 86400  # 24 hours in seconds


def cache_get(key: str) -> str | None:
    if key in _cache:
        val, ts = _cache[key]
        if _time.time() - ts < CACHE_TTL:
            return val
        del _cache[key]
    return None


def cache_set(key: str, val: str) -> None:
    _cache[key] = (val, _time.time())


def make_cache_key(data: str) -> str:
    return hashlib.sha256(data.encode()).hexdigest()


# ── Request Models ───────────────────────────────────────────────────────────

class ItineraryRequest(BaseModel):
    destination: str
    durationDays: int
    budget: str
    travelerType: str
    interests: str
    useGrounding: bool = False


class PackingRequest(BaseModel):
    destination: str
    weatherType: str
    tripDuration: int
    mainActivities: str


class FoodRequest(BaseModel):
    region: str
    alreadyShown: list[str] = []


class ChatMessage(BaseModel):
    content: str
    isUser: bool


class ChatRequest(BaseModel):
    message: str
    history: list[ChatMessage] = []


# ── Gemini async helper ──────────────────────────────────────────────────────

async def call_gemini(prompt: str, json_mode: bool = True) -> str:
    errors = []
    for model_name in MODELS:
        try:
            config = genai.GenerationConfig(
                temperature=0.3,
                max_output_tokens=8192,
                response_mime_type="application/json" if json_mode else "text/plain",
            )
            model = genai.GenerativeModel(
                model_name=model_name,
                generation_config=config,
            )
            response = await model.generate_content_async(prompt)
            return response.text
        except Exception as e:
            msg = str(e)
            if "429" in msg or "quota" in msg.lower():
                await asyncio.sleep(1)
            errors.append(f"{model_name}: {msg[:100]}")
            continue
    raise HTTPException(
        status_code=503,
        detail="All Gemini models failed:\n" + "\n".join(errors)
    )


def clean_json(raw: str) -> str:
    # Strip markdown fences: ```json ... ``` or ``` ... ```
    raw = raw.strip()
    if raw.startswith("```"):
        raw = raw.split("\n", 1)[-1]
    if raw.endswith("```"):
        raw = raw.rsplit("```", 1)[0]
    raw = raw.strip()
    # Find actual JSON boundaries
    start = next((i for i, c in enumerate(raw) if c in ('{', '[')), -1)
    end = next((i for i in range(len(raw) - 1, -1, -1) if raw[i] in ('}', ']')), -1)
    if start != -1 and end != -1 and end > start:
        return raw[start:end + 1]
    return raw.strip()


# ── Endpoints ────────────────────────────────────────────────────────────────

@app.get("/")
async def root():
    return {"status": "TripGenie API is running", "version": "1.0.0"}


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/itinerary")
async def generate_itinerary(req: ItineraryRequest):
    cache_key = make_cache_key(req.model_dump_json())
    if cached := cache_get(cache_key):
        return {"result": cached}

    act_count = 4 if req.durationDays <= 1 else (5 if req.durationDays == 2 else 6)
    prompt = f"""Create a {req.durationDays}-day itinerary for {req.destination}.
Traveler:{req.travelerType}|Budget:{req.budget}|Interests:{req.interests}

Output ONLY JSON no markdown:
{{"title":"Trip title","description":"Brief summary","days":[{{"dayNumber":1,"theme":"Theme","activities":[{{"time":"09:00 AM","title":"Activity name","description":"Short description","location":"Place name","estimatedCost":"Free","lat":13.34,"lng":74.74}}]}}],"budgetBreakdown":{{"accommodation":40,"activities":20,"food":25,"transport":15}},"localFoods":["f1","f2","f3"],"recommendedRestaurants":["r1","r2"]}}

IMPORTANT: {req.durationDays} days, {act_count} activities/day, REAL GPS lat/lng for {req.destination} (not 0.0), budgetBreakdown sums=100."""

    raw = await call_gemini(prompt)
    cleaned = clean_json(raw)
    try:
        json.loads(cleaned)
    except Exception:
        raise HTTPException(status_code=500, detail=f"Invalid JSON from model: {cleaned[:300]}")

    cache_set(cache_key, cleaned)
    return {"result": cleaned}


@app.post("/packing")
async def generate_packing(req: PackingRequest):
    cache_key = make_cache_key(req.model_dump_json())
    if cached := cache_get(cache_key):
        return {"result": cached}

    prompt = f"""Packing list for: {req.destination}, {req.weatherType}, {req.tripDuration} days, activities: {req.mainActivities}
Respond ONLY with valid JSON array (no markdown):
[{{"name":"Clothing","icon":"👕","items":[{{"name":"item"}}]}},{{"name":"Footwear","icon":"👟","items":[{{"name":"item"}}]}},{{"name":"Toiletries","icon":"🧴","items":[{{"name":"item"}}]}},{{"name":"Electronics","icon":"📱","items":[{{"name":"item"}}]}},{{"name":"Documents","icon":"📄","items":[{{"name":"item"}}]}},{{"name":"Health","icon":"💊","items":[{{"name":"item"}}]}}]
6-10 specific items per category for {req.weatherType} weather and {req.mainActivities}."""

    raw = await call_gemini(prompt)
    cleaned = clean_json(raw)
    try:
        json.loads(cleaned)
    except Exception:
        raise HTTPException(status_code=500, detail=f"Invalid JSON from model: {cleaned[:300]}")

    cache_set(cache_key, cleaned)
    return {"result": cleaned}


@app.post("/food")
async def generate_food(req: FoodRequest):
    # Sort alreadyShown so order doesn't affect cache key
    sorted_req = req.model_dump()
    sorted_req["alreadyShown"] = sorted(sorted_req["alreadyShown"])
    cache_key = make_cache_key(json.dumps(sorted_req, sort_keys=True))
    if cached := cache_get(cache_key):
        return {"result": cached}

    exclude = f"Exclude: {', '.join(req.alreadyShown)}." if req.alreadyShown else ""
    prompt = f"""List 6 famous authentic local dishes from {req.region} that tourists must try. {exclude}

Respond ONLY with a valid JSON array. No markdown, no backticks, no extra text:
[{{"name":"Dish Name","restaurant":"RESTAURANT NAME","description":"Two mouthwatering sentences describing taste and texture.","bestSpot":"Best restaurant name and street/area","icon":"SINGLE_EMOJI"}}]

Rules:
- Pick FAMOUS well-known local specialties from {req.region}
- For icon: choose the single most visually accurate emoji for the dish
- Each dish must be a real local specialty, not generic
- Restaurant must be a real local restaurant name from {req.region}
- {exclude}"""

    raw = await call_gemini(prompt)
    cleaned = clean_json(raw)
    try:
        json.loads(cleaned)
    except Exception:
        raise HTTPException(status_code=500, detail=f"Invalid JSON from model: {cleaned[:300]}")

    cache_set(cache_key, cleaned)
    return {"result": cleaned}


@app.post("/chat")
async def chat(req: ChatRequest):
    hist = "\n".join(
        f"{'User' if m.isUser else 'Assistant'}: {m.content}"
        for m in req.history[-4:]
    )
    prompt = (
        "You are Concierge Genie, a friendly AI travel assistant. "
        "Give practical travel advice in 3-4 sentences.\n"
        f"{hist}\nUser: {req.message}"
    )
    reply = await call_gemini(prompt, json_mode=False)
    return {"reply": reply}
