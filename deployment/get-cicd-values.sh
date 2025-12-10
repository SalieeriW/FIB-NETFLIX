#!/bin/bash
# Script para obtener valores necesarios para CI/CD

echo "════════════════════════════════════════"
echo "🔍 Obteniendo Valores para CI/CD"
echo "════════════════════════════════════════"
echo ""

# Verificar OCI CLI
if ! command -v oci &> /dev/null; then
    echo "❌ OCI CLI no está instalado"
    exit 1
fi

echo "✅ OCI CLI encontrado"
echo ""

# 1. Namespace
echo "1️⃣  OCI_TENANCY_NAMESPACE:"
NAMESPACE=$(oci os ns get --query 'data' --raw-output 2>/dev/null)
if [ -n "$NAMESPACE" ]; then
    echo "   ✅ $NAMESPACE"
else
    echo "   ❌ No se pudo obtener. Verifica OCI CLI config"
fi
echo ""

# 2. Fingerprint
echo "2️⃣  OCI_FINGERPRINT:"
FINGERPRINT=$(cat ~/.oci/config 2>/dev/null | grep "^fingerprint" | head -1 | cut -d'=' -f2 | tr -d ' ')
if [ -n "$FINGERPRINT" ]; then
    echo "   ✅ $FINGERPRINT"
else
    echo "   ❌ No encontrado en ~/.oci/config"
fi
echo ""

# 3. Private Key
echo "3️⃣  OCI_PRIVATE_KEY:"
PRIVATE_KEY_FILE=$(ls -1 ~/.oci/*.pem 2>/dev/null | grep -v "_public" | grep -v "public.pem" | head -1)
if [ -n "$PRIVATE_KEY_FILE" ] && [ -f "$PRIVATE_KEY_FILE" ]; then
    echo "   ✅ Archivo: $PRIVATE_KEY_FILE"
    echo "   📋 Copia el contenido completo:"
    echo "   ────────────────────────────────────────────"
    cat "$PRIVATE_KEY_FILE"
    echo "   ────────────────────────────────────────────"
else
    echo "   ❌ No se encontró private key"
fi
echo ""

# 4. User OCID
echo "4️⃣  OCI_USER_OCID:"
USER_OCID=$(cat ~/.oci/config 2>/dev/null | grep "^user=" | head -1 | cut -d'=' -f2 | tr -d ' ')
if [ -n "$USER_OCID" ]; then
    echo "   ✅ $USER_OCID"
else
    echo "   ❌ No encontrado"
fi
echo ""

# 5. Tenancy OCID
echo "5️⃣  OCI_TENANCY_OCID:"
TENANCY_OCID=$(cat ~/.oci/config 2>/dev/null | grep "^tenancy=" | head -1 | cut -d'=' -f2 | tr -d ' ')
if [ -n "$TENANCY_OCID" ]; then
    echo "   ✅ $TENANCY_OCID"
else
    echo "   ❌ No encontrado"
fi
echo ""

# 6. Region
echo "6️⃣  OCI_REGION / OCI_REGISTRY_REGION:"
REGION=$(cat ~/.oci/config 2>/dev/null | grep "^region=" | head -1 | cut -d'=' -f2 | tr -d ' ')
if [ -n "$REGION" ]; then
    echo "   ✅ $REGION"
else
    echo "   ❌ No encontrado"
fi
echo ""

# 7. Username
echo "7️⃣  OCI_USERNAME:"
echo "   ⚠️  Necesitas obtenerlo manualmente:"
echo "   - OCI Console → User Settings → User Information"
echo "   - O usa tu email de OCI"
echo ""

# 8. Auth Token
echo "8️⃣  OCI_AUTH_TOKEN:"
echo "   ⚠️  Necesitas crearlo manualmente:"
echo "   - OCI Console → User Settings → Auth Tokens"
echo "   - Generate Token"
echo "   - ⚠️  GUÁRDALO (solo se muestra una vez!)"
echo ""

# 9. Kubeconfig
echo "9️⃣  KUBECONFIG:"
echo "   ⚠️  Se obtiene después de crear el cluster:"
echo "   oci ce cluster create-kubeconfig --cluster-id <cluster-id> --file kubeconfig"
echo ""

echo "════════════════════════════════════════"
echo "📋 Resumen - Secrets para GitHub"
echo "════════════════════════════════════════"
echo ""
echo "Ve a: GitHub Repo → Settings → Secrets and variables → Actions"
echo ""
echo "Agrega estos secrets:"
echo ""
[ -n "$FINGERPRINT" ] && echo "OCI_FINGERPRINT = $FINGERPRINT"
[ -n "$USER_OCID" ] && echo "OCI_USER_OCID = $USER_OCID"
[ -n "$TENANCY_OCID" ] && echo "OCI_TENANCY_OCID = $TENANCY_OCID"
[ -n "$REGION" ] && echo "OCI_REGION = $REGION"
[ -n "$REGION" ] && echo "OCI_REGISTRY_REGION = $REGION"
[ -n "$NAMESPACE" ] && echo "OCI_TENANCY_NAMESPACE = $NAMESPACE"
echo "OCI_PRIVATE_KEY = [contenido completo del archivo .pem]"
echo "OCI_USERNAME = [tu username de OCI]"
echo "OCI_AUTH_TOKEN = [token generado en OCI Console]"
echo "KUBECONFIG = [después de crear el cluster]"
echo ""

