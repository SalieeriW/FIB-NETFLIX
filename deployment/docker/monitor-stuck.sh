#!/bin/bash
# Script to monitor if Python service is stuck processing

echo "════════════════════════════════════════"
echo "MONITOREO DE PROCESO"
echo "════════════════════════════════════════"
echo ""

# Check Python service logs
echo "1. Últimas 5 líneas de Python service:"
docker logs vidstream-python --tail 5 2>&1
echo ""

# Check if there's activity in last 2 minutes
PYTHON_ACTIVITY=$(docker logs vidstream-python --since 2m 2>&1 | wc -l)
echo "2. Actividad en últimos 2 minutos: $PYTHON_ACTIVITY líneas"

if [ "$PYTHON_ACTIVITY" -eq 0 ]; then
    echo "   ⚠️  SIN ACTIVIDAD - Posiblemente atascado"
else
    echo "   ✅ Hay actividad"
fi
echo ""

# Check CPU usage
echo "3. Uso de CPU (últimos 5 segundos):"
docker stats vidstream-python vidstream-ollama --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>&1
echo ""

# Check Ollama logs for generation requests
echo "4. Requests a Ollama (últimos 2 minutos):"
OLLAMA_REQUESTS=$(docker logs vidstream-ollama --since 2m 2>&1 | grep -E "(POST|generate)" | wc -l)
echo "   Requests encontrados: $OLLAMA_REQUESTS"

if [ "$OLLAMA_REQUESTS" -eq 0 ]; then
    echo "   ⚠️  Sin requests a Ollama - El proceso puede estar atascado"
else
    echo "   ✅ Ollama está recibiendo requests"
fi
echo ""

# Check for errors
echo "5. Errores recientes:"
ERRORS=$(docker logs vidstream-python --since 5m 2>&1 | grep -iE "(error|exception|traceback|failed)" | tail -5)
if [ -z "$ERRORS" ]; then
    echo "   ✅ Sin errores recientes"
else
    echo "   ❌ Errores encontrados:"
    echo "$ERRORS"
fi
echo ""

# Summary
echo "════════════════════════════════════════"
echo "RESUMEN"
echo "════════════════════════════════════════"

if [ "$PYTHON_ACTIVITY" -eq 0 ] && [ "$OLLAMA_REQUESTS" -eq 0 ]; then
    echo "❌ PROCESO PROBABLEMENTE ATASCADO"
    echo ""
    echo "Recomendaciones:"
    echo "  1. Ver logs completos: docker logs vidstream-python --tail 100"
    echo "  2. Reiniciar servicio: docker-compose restart python-service"
    echo "  3. Verificar Ollama: docker logs vidstream-ollama --tail 50"
else
    echo "✅ PROCESO ACTIVO"
    echo ""
    echo "Para monitorear en tiempo real:"
    echo "  docker logs -f vidstream-python"
fi

