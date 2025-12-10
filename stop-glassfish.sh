#!/bin/bash

# Script para detener GlassFish

GLASSFISH_HOME="/Users/swang/opt/glassfish7"

if [ ! -f "$GLASSFISH_HOME/bin/asadmin" ]; then
    echo "❌ GlassFish no encontrado en $GLASSFISH_HOME"
    exit 1
fi

echo "🛑 Deteniendo GlassFish..."

$GLASSFISH_HOME/bin/asadmin stop-database
$GLASSFISH_HOME/bin/asadmin stop-domain

echo "✅ GlassFish detenido"

