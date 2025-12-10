#!/bin/bash

# Script para iniciar GlassFish

GLASSFISH_HOME="/Users/swang/opt/glassfish7"

if [ ! -f "$GLASSFISH_HOME/bin/asadmin" ]; then
    echo "❌ GlassFish no encontrado en $GLASSFISH_HOME"
    echo "   Verifica la instalación o actualiza GLASSFISH_HOME en este script"
    exit 1
fi

echo "🚀 Iniciando GlassFish..."
echo ""

# Iniciar dominio
$GLASSFISH_HOME/bin/asadmin start-domain

# Esperar un momento
sleep 2

# Iniciar base de datos
$GLASSFISH_HOME/bin/asadmin start-database

echo ""
echo "✅ GlassFish iniciado"
echo "   Admin Console: http://localhost:4848"
echo "   Puerto: 8080"
echo ""
echo "📝 Para detener: ./stop-glassfish.sh"

