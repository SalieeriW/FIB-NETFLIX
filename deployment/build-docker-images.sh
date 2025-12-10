#!/bin/bash
# Build all Docker images for VidStream

set -e

echo "════════════════════════════════════════"
echo "📦 Construyendo Imágenes Docker"
echo "════════════════════════════════════════"
echo ""

# Check Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker no está corriendo. Por favor inicia Docker Desktop."
    exit 1
fi

echo "✅ Docker está corriendo"
echo ""

# Build Python Service
echo "1️⃣  Construyendo Python Service..."
cd "$(dirname "$0")/../.."
docker build -t vidstream-python:latest \
    -f deployment/docker/python-service/Dockerfile . || {
    echo "❌ Error construyendo Python Service"
    exit 1
}
echo "✅ Python Service construido"
echo ""

# Build Java REST Service (need to build WAR first)
echo "2️⃣  Construyendo Java REST Service..."
echo "   Primero construyendo WAR file..."
cd rest-service
if [ ! -f "target/practica5-rest-service.war" ]; then
    echo "   Compilando con Maven..."
    mvn clean package -DskipTests || {
        echo "❌ Error compilando REST service"
        exit 1
    }
fi
cd ..
docker build -t vidstream-rest:latest \
    -f deployment/docker/java-rest-service/Dockerfile . || {
    echo "❌ Error construyendo Java REST Service"
    exit 1
}
echo "✅ Java REST Service construido"
echo ""

# Build Web Client (need to build WAR first)
echo "3️⃣  Construyendo Web Client..."
echo "   Primero construyendo WAR file..."
cd web-client
if [ ! -f "target/practica5-web-client.war" ]; then
    echo "   Compilando con Maven..."
    mvn clean package -DskipTests || {
        echo "❌ Error compilando Web Client"
        exit 1
    }
fi
cd ..
docker build -t vidstream-web:latest \
    -f deployment/docker/web-client/Dockerfile . || {
    echo "❌ Error construyendo Web Client"
    exit 1
}
echo "✅ Web Client construido"
echo ""

echo "════════════════════════════════════════"
echo "✅ Todas las imágenes construidas!"
echo "════════════════════════════════════════"
echo ""
echo "Imágenes creadas:"
docker images | grep vidstream
echo ""
echo "Próximo paso: Probar con Docker Compose"
echo "  cd deployment/docker"
echo "  docker-compose up -d"

