#!/bin/bash

# Script para iniciar el servicio Python RAG

cd "$(dirname "$0")/python-services"

# Activar entorno virtual
if [ ! -d "venv" ]; then
    echo "❌ Virtual environment no encontrado. Ejecuta primero:"
    echo "   cd python-services && python3 -m venv venv && source venv/bin/activate && pip install -r requirements.txt"
    exit 1
fi

source venv/bin/activate

# Deshabilitar telemetría
export ANONYMIZED_TELEMETRY=False
export DO_NOT_TRACK=1
export CHROMA_TELEMETRY=0

# Usar puerto 5001 si 5000 está ocupado
PORT=${PYTHON_SERVICE_PORT:-5001}

echo "🚀 Iniciando Python RAG Service en http://localhost:${PORT}"
echo "📝 Presiona Ctrl+C para detener"
echo ""

uvicorn main:app --host 0.0.0.0 --port ${PORT} --reload

