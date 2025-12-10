#!/bin/bash

echo "🔍 Verificando si Ollama está instalado..."

# Verificar si Ollama está en el PATH
if command -v ollama &> /dev/null; then
    echo "✅ Ollama ya está instalado"
    ollama --version
    exit 0
fi

# Verificar si Homebrew está instalado
if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew no está instalado"
    echo ""
    echo "Para instalar Ollama, primero necesitas Homebrew:"
    echo "  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
    echo ""
    echo "O descarga Ollama manualmente desde: https://ollama.ai/download"
    exit 1
fi

echo "📦 Instalando Ollama con Homebrew..."
brew install ollama

if [ $? -eq 0 ]; then
    echo "✅ Ollama instalado correctamente"
    echo ""
    echo "🚀 Para iniciar Ollama, ejecuta:"
    echo "   ollama serve"
    echo ""
    echo "📥 Para descargar el modelo necesario, ejecuta (en otra terminal):"
    echo "   ollama pull qwen2.5:7b"
else
    echo "❌ Error instalando Ollama"
    exit 1
fi

