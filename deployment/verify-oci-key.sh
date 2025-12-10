#!/bin/bash
# Verify OCI key fingerprint

echo "════════════════════════════════════════"
echo "🔍 OCI Key Verification"
echo "════════════════════════════════════════"
echo ""

# Check DEFAULT profile
echo "DEFAULT Profile:"
echo "  Config fingerprint: c6:a6:35:0c:c3:87:42:fc:b1:20:d9:97:99:dd:7e:48"
echo "  Key file: ~/.oci/oci-mac.pem"
echo "  Public key file: ~/.oci/oci-mac_public.pem"
echo ""

# Show public key
echo "Public Key to upload (if not already uploaded):"
echo "────────────────────────────────────────────"
cat ~/.oci/oci-mac_public.pem
echo "────────────────────────────────────────────"
echo ""

echo "📋 Steps to fix:"
echo "1. Go to OCI Console → User Settings → API Keys"
echo "2. Check if fingerprint 'c6:a6:35:0c:c3:87:42:fc:b1:20:d9:97:99:dd:7e:48' exists"
echo "3. If NOT, upload the public key above"
echo "4. If YES, the fingerprint might be wrong - delete and re-upload"
echo ""

echo "💡 Common issues:"
echo "- Fingerprint mismatch (config vs uploaded key)"
echo "- Wrong public key uploaded"
echo "- Key deleted from OCI Console"
echo "- Passphrase issue (try without passphrase)"
echo ""

