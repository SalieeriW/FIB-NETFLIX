# ============================================
# STEP 4.4: Add Metrics to Python Service
# ============================================
# This shows how to add Prometheus metrics to your FastAPI app

# Add to requirements.txt:
# prometheus-client==0.19.0

# Add to main.py:
"""
from prometheus_client import Counter, Histogram, Gauge, generate_latest
from fastapi import Response
from fastapi.responses import PlainTextResponse

# Define metrics
REQUEST_COUNT = Counter(
    'http_requests_total',
    'Total HTTP requests',
    ['method', 'endpoint', 'status']
)

REQUEST_DURATION = Histogram(
    'http_request_duration_seconds',
    'HTTP request duration',
    ['method', 'endpoint']
)

ACTIVE_USERS = Gauge(
    'active_users',
    'Number of active users'
)

ML_INFERENCE_TIME = Histogram(
    'ml_inference_seconds',
    'ML model inference time',
    ['model', 'operation']
)

# Add middleware to track requests
@app.middleware("http")
async def track_requests(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    duration = time.time() - start_time
    
    REQUEST_COUNT.labels(
        method=request.method,
        endpoint=request.url.path,
        status=response.status_code
    ).inc()
    
    REQUEST_DURATION.labels(
        method=request.method,
        endpoint=request.url.path
    ).observe(duration)
    
    return response

# Expose metrics endpoint
@app.get("/metrics")
async def metrics():
    return PlainTextResponse(generate_latest())

# Track ML operations
# In chat_service.py:
import time
from prometheus_client import Histogram

CHAT_INFERENCE_TIME = Histogram(
    'chat_inference_seconds',
    'Chat LLM inference time'
)

def chat(self, course_id, question, language):
    with CHAT_INFERENCE_TIME.time():
        # Your existing chat code
        answer = self.llm.invoke(prompt)
    return answer
"""

