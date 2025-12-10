# OCI Authentication Troubleshooting

## Current Issue: 401 NotAuthenticated

### Problem
The fingerprint in your config doesn't match what's uploaded in OCI Console.

### Solution Steps

1. **Verify what's in OCI Console:**
   - Go to: https://cloud.oracle.com
   - User Settings → API Keys
   - Check all uploaded keys and their fingerprints

2. **Upload the correct public key:**
   - The public key for DEFAULT profile is shown above
   - Fingerprint should be: `d7:03:a3:cd:47:cb:9e:2d:47:f2:9c:cc:a0:d1:73:15`
   - If this fingerprint is NOT in OCI Console, upload the key
   - If a different key with this fingerprint exists, delete it and re-upload

3. **Check for passphrase issues:**
   - Your DEFAULT profile has a passphrase: `W110120130h`
   - Make sure this is correct
   - If unsure, you can regenerate without passphrase

## Quick Fix Options

### Option 1: Use SALIEEERI Profile (No Passphrase)
The SALIEEERI profile doesn't have a passphrase, which is simpler:

```bash
# Upload mac-oci-2_public.pem to OCI Console first
# Then test:
oci iam region list --profile SALIEEERI
```

### Option 2: Fix DEFAULT Profile
1. Upload `oci-mac_public.pem` to OCI Console
2. Verify fingerprint matches: `d7:03:a3:cd:47:cb:9e:2d:47:f2:9c:cc:a0:d1:73:15`
3. Test: `oci iam region list --profile DEFAULT`

### Option 3: Create New Profile (Simplest)
```bash
# Generate new key without passphrase
oci setup config
# When asked:
# - Create new profile name
# - Use existing key: n
# - Generate new key: y
# - No passphrase: N/A
# - Upload the new public key to OCI Console
```

## Verification Commands

```bash
# Test DEFAULT profile
oci iam region list --profile DEFAULT

# Test SALIEEERI profile  
oci iam region list --profile SALIEEERI

# List all profiles
grep -E "^\[.*\]" ~/.oci/config

# View specific profile
grep -A 10 "^\[DEFAULT\]" ~/.oci/config
```

## Common Mistakes

1. **Wrong public key uploaded** - Make sure the public key matches the private key
2. **Fingerprint mismatch** - Config fingerprint must match OCI Console fingerprint
3. **Key deleted** - If you deleted a key from OCI Console, you need to re-upload it
4. **Wrong region** - Make sure region in config matches where you uploaded the key
5. **Passphrase wrong** - If key has passphrase, it must be correct

## Next Steps

Once authentication works:
1. ✅ Test: `oci iam region list` should return regions
2. ✅ Update `terraform.tfvars` with your OCIDs
3. ✅ Continue with deployment

