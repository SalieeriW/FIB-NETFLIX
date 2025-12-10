#!/bin/bash

# Script para compilar y desplegar los WARs

GLASSFISH_HOME="/Users/swang/opt/glassfish7"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ ! -f "$GLASSFISH_HOME/bin/asadmin" ]; then
    echo "❌ GlassFish no encontrado en $GLASSFISH_HOME"
    exit 1
fi

echo "🔨 Compilando proyectos..."
cd "$PROJECT_DIR"
mvn clean package

if [ $? -ne 0 ]; then
    echo "❌ Error al compilar"
    exit 1
fi

echo ""
echo "📦 Desplegando WARs..."

# Desplegar REST Service
echo "   - Desplegando REST Service..."
$GLASSFISH_HOME/bin/asadmin deploy --force=true rest-service/target/practica5-rest-service.war

# Desplegar Web Client
echo "   - Desplegando Web Client..."
$GLASSFISH_HOME/bin/asadmin deploy --force=true web-client/target/practica5-web-client.war

echo ""
echo "✅ Despliegue completado"
echo "   REST API: http://localhost:8080/practica5-rest-service/resources/"
echo "   Web App: http://localhost:8080/practica5-web-client/"

