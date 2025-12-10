#!/bin/bash
# Quick fix script for OCI configuration

echo "════════════════════════════════════════"
echo "🔧 OCI Configuration Fix"
echo "════════════════════════════════════════"
echo ""

# Show available keys
echo "Available keys:"
ls -1 ~/.oci/*.pem ~/.oci/*_public.pem 2>/dev/null | grep -v "private" | while read key; do
    if [[ "$key" == *"_public.pem" ]] || [[ "$key" == *"public.pem" ]]; then
        echo "  📄 Public: $key"
        echo "     Fingerprint: $(openssl rsa -pubin -in "$key" -outform DER 2>/dev/null | openssl dgst -sha256 -binary | openssl enc -base64 | cut -c1-16 | sed 's/\(..\)/\1:/g' | sed 's/:$//' || echo 'Could not calculate')"
    fi
done

echo ""
echo "Current config profiles:"
grep -E "^\[.*\]" ~/.oci/config 2>/dev/null || echo "  No config file found"

echo ""
echo "════════════════════════════════════════"
echo "📋 Action Required:"
echo "════════════════════════════════════════"
echo ""
echo "You need to upload ONE of these public keys to OCI Console:"
echo ""
echo "1. For DEFAULT profile:"
echo "   cat ~/.oci/oci-mac_public.pem  # (if exists)"
echo ""
echo "2. For SALIEEERI profile (NEW):"
echo "   cat ~/.oci/mac-oci-2_public.pem"
echo ""
echo "Steps:"
echo "1. Copy the public key content"
echo "2. Go to: https://cloud.oracle.com"
echo "3. User Settings → API Keys → Add API Key"
echo "4. Paste the key and save"
echo "5. Test: oci iam region list --profile <PROFILE_NAME>"
echo ""

