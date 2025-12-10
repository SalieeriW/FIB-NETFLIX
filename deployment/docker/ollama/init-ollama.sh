#!/bin/bash
# Initialize Ollama and download required models

set -e

echo "🔍 Checking if Ollama is ready..."
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if curl -f http://localhost:11434/api/tags > /dev/null 2>&1; then
        echo "✅ Ollama is ready!"
        break
    fi
    attempt=$((attempt + 1))
    echo "⏳ Waiting for Ollama to be ready... (attempt $attempt/$max_attempts)"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ Ollama failed to start after $max_attempts attempts"
    exit 1
fi

echo ""
echo "📦 Checking for required models..."

MODEL="qwen2.5:7b"

# Check if model exists
if ollama list | grep -q "$MODEL"; then
    echo "✅ Model $MODEL already exists"
else
    echo "📥 Downloading model $MODEL (this may take several minutes)..."
    ollama pull "$MODEL"
    echo "✅ Model $MODEL downloaded successfully"
fi

# Pre-load the model to avoid cold start delays
# Use the /api/generate endpoint with keep_alive to load and keep model in memory
echo "🔄 Pre-loading model $MODEL into memory (this may take 15-20 seconds)..."
# Use curl to call the API with keep_alive parameter to keep model loaded
curl -X POST http://localhost:11434/api/generate \
  -H "Content-Type: application/json" \
  -d "{\"model\": \"$MODEL\", \"prompt\": \"test\", \"stream\": false, \"keep_alive\": \"5m\"}" \
  > /dev/null 2>&1 || echo "⚠️  Pre-load request failed, but model is available"
echo "✅ Model initialization complete"

echo ""
echo "📋 Available models:"
ollama list

echo ""
echo "✅ Ollama initialization complete!"

