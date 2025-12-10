#!/bin/bash

echo "🔍 Verificando estado del servicio Python..."
echo ""

# Verificar si el proceso está corriendo
if ps aux | grep -q "[u]vicorn.*main:app"; then
    echo "✅ Proceso uvicorn está corriendo"
    ps aux | grep "[u]vicorn.*main:app" | grep -v grep | head -1 | awk '{print "   PID:", $2, "| CPU:", $3"%", "| Mem:", $4"%"}'
else
    echo "❌ Proceso uvicorn NO está corriendo"
    exit 1
fi

echo ""

# Verificar si el puerto está escuchando
if lsof -i :5001 2>/dev/null | grep -q LISTEN; then
    echo "✅ Puerto 5001 está escuchando"
else
    echo "⏳ Puerto 5001 aún NO está escuchando (cargando modelos...)"
fi

echo ""

# Intentar hacer una petición HTTP
echo "🌐 Probando endpoint /api/health..."
response=$(curl -s -w "\n%{http_code}" http://localhost:5001/api/health 2>&1)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" = "200" ]; then
    echo "✅ Servicio RESPONDIENDO correctamente"
    echo "   Respuesta: $body"
    echo ""
    echo "🎉 ¡El servicio está LISTO!"
elif [ "$http_code" = "000" ] || [ -z "$http_code" ]; then
    echo "⏳ Servicio aún NO responde (cargando modelos, espera unos minutos...)"
    echo ""
    echo "💡 Tip: El primer inicio puede tardar 2-5 minutos mientras descarga modelos"
else
    echo "⚠️  Servicio responde con código HTTP: $http_code"
    echo "   Respuesta: $body"
fi

