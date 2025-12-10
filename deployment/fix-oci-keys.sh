#!/bin/bash
# Fix OCI key mismatch by generating new key pair

echo "════════════════════════════════════════"
echo "🔧 OCI Key Fix - Generate New Key Pair"
echo "════════════════════════════════════════"
echo ""
echo "This will:"
echo "1. Generate a new private/public key pair"
echo "2. Update your config"
echo "3. Show you the public key to upload to OCI Console"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelled."
    exit 1
fi

# Generate new key pair
KEY_NAME="oci-default-$(date +%s)"
PRIVATE_KEY="$HOME/.oci/${KEY_NAME}.pem"
PUBLIC_KEY="$HOME/.oci/${KEY_NAME}_public.pem"

echo ""
echo "Generating new key pair..."
openssl genrsa -out "$PRIVATE_KEY" 2048
openssl rsa -pubout -in "$PRIVATE_KEY" -out "$PUBLIC_KEY"

# Calculate fingerprint
FINGERPRINT=$(openssl rsa -pubin -in "$PUBLIC_KEY" -outform DER 2>/dev/null | openssl dgst -sha256 | awk '{print $2}' | head -c 32 | sed 's/\(..\)/\1:/g' | sed 's/:$//')

echo "✅ Key pair generated!"
echo ""
echo "Private key: $PRIVATE_KEY"
echo "Public key: $PUBLIC_KEY"
echo "Fingerprint: $FINGERPRINT"
echo ""

# Update config
echo "Updating config..."
sed -i.bak6 "s|key_file=.*|key_file=$PRIVATE_KEY|" ~/.oci/config
sed -i.bak7 "s|fingerprint=.*|fingerprint=$FINGERPRINT|" ~/.oci/config

echo "✅ Config updated!"
echo ""
echo "════════════════════════════════════════"
echo "📋 NEXT STEP: Upload Public Key to OCI"
echo "════════════════════════════════════════"
echo ""
echo "1. Go to: https://cloud.oracle.com"
echo "2. User Settings → API Keys"
echo "3. DELETE the old key (fingerprint: c6:a6:35:0c:c3:87:42:fc:b1:20:d9:97:99:dd:7e:48)"
echo "4. Click 'Add API Key' → 'Paste Public Key'"
echo "5. Paste this key:"
echo ""
echo "────────────────────────────────────────────"
cat "$PUBLIC_KEY"
echo "────────────────────────────────────────────"
echo ""
echo "6. Verify fingerprint matches: $FINGERPRINT"
echo "7. Test: oci iam region list --profile DEFAULT"
echo ""

