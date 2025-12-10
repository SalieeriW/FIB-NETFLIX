# ============================================
# STEP 1.1: Python ML Service Dockerfile
# ============================================
# This Dockerfile creates a container for our Python FastAPI service
# that handles ML operations (Whisper, embeddings, RAG, chat)

# --- Stage 1: Base Image ---
# We use Python 3.9 slim to keep the image small
# "slim" means minimal OS packages - saves space
FROM python:3.9-slim as base

# Set working directory inside container
# All commands run from this directory
WORKDIR /app

# --- Stage 2: Dependencies ---
# Install system dependencies needed for ML libraries
# FFmpeg is needed for Whisper audio processing
# Build tools needed for compiling some Python packages
FROM base as dependencies

# Install system packages
# -y flag means "yes to all prompts" (non-interactive)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements file first
# Docker caches layers - if requirements.txt doesn't change,
# it won't reinstall packages (saves time)
COPY python-services/requirements.txt .

# Install Python dependencies
# --no-cache-dir saves space by not storing pip cache
RUN pip install --no-cache-dir -r requirements.txt

# --- Stage 3: Application ---
# Final stage - copy application code
FROM dependencies as app

# Copy application code
# Copy from python-services/ directory
COPY python-services/ .

# Expose port 5001
# This tells Docker (and Kubernetes) which port the app uses
EXPOSE 5001

# Set environment variables
# These can be overridden in Kubernetes
ENV PYTHON_SERVICE_PORT=5001
ENV PYTHONUNBUFFERED=1

# Health check
# Kubernetes uses this to know if the container is healthy
# Returns 200 if service is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:5001/api/health')"

# Run the application
# uvicorn is the ASGI server for FastAPI
# --host 0.0.0.0 means "listen on all network interfaces"
# This is important for containers - they need to accept external connections
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5001"]

