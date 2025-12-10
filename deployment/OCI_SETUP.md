# OCI Setup Guide - Step by Step

## Step 1: View Your Public Key

First, let's see your public key so you can copy it:

```bash
cat ~/.oci/oci_api_key_public.pem
```

Copy the entire output (including `-----BEGIN PUBLIC KEY-----` and `-----END PUBLIC KEY-----`).

## Step 2: Upload to OCI Console

1. **Go to Oracle Cloud Console:**
   - Visit: https://cloud.oracle.com
   - Sign in with your account

2. **Navigate to API Keys:**
   - Click your **user icon** (top right)
   - Select **User Settings**
   - In the left menu, click **API Keys**

3. **Add API Key:**
   - Click **Add API Key** button
   - Select **Paste Public Key**
   - Paste the public key you copied from Step 1
   - Click **Add**

4. **Copy the Fingerprint:**
   - After adding, you'll see a **Configuration File Preview**
   - **IMPORTANT**: Copy the **fingerprint** (looks like: `12:34:56:78:90:ab:cd:ef:12:34:56:78:90:ab:cd:ef`)
   - Save this - you'll need it for configuration

## Step 3: Get Your OCIDs

You need three OCIDs:

### A. User OCID
1. In OCI Console, click your **user icon** (top right)
2. Click **User Settings**
3. Under **User Information**, find **OCID**
4. Click the **copy icon** next to it
5. Save it somewhere

### B. Tenancy OCID
1. Click the **hamburger menu** (☰) in top left
2. Go to **Administration** → **Tenancy Details**
3. Find **OCID** under **Tenancy Information**
4. Click the **copy icon**
5. Save it

### C. Compartment OCID (optional, but recommended)
1. Click **hamburger menu** (☰)
2. Go to **Identity & Security** → **Compartments**
3. Find your compartment (or create one)
4. Click on it
5. Copy the **OCID**

## Step 4: Configure OCI CLI

Now configure the CLI with your information:

```bash
oci setup config
```

You'll be prompted for:
1. **Enter a location for your config** → Press Enter (default: `~/.oci/config`)
2. **Enter a user OCID** → Paste your User OCID from Step 3A
3. **Enter a tenancy OCID** → Paste your Tenancy OCID from Step 3B
4. **Enter a region** → Type your region (e.g., `us-ashburn-1`, `us-phoenix-1`)
   - Common regions:
     - `us-ashburn-1` (US East)
     - `us-phoenix-1` (US West)
     - `eu-frankfurt-1` (Europe)
     - `uk-london-1` (UK)
5. **Enter the path to your private key file** → `~/.oci/oci_api_key.pem`
6. **Enter the fingerprint for your key** → Paste the fingerprint from Step 2

## Step 5: Test Configuration

Verify everything works:

```bash
# Test OCI CLI connection
oci iam region list

# Should return a list of regions. If you see an error, check:
# - API key fingerprint matches
# - Private key path is correct
# - OCIDs are correct
```

## Step 6: Get Compartment OCID (if needed)

If you want to use a specific compartment:

```bash
# List compartments
oci iam compartment list

# Or get root compartment
oci iam compartment get --compartment-id <tenancy-ocid>
```

## Step 7: Update Terraform Variables

Now update your Terraform configuration:

```bash
cd deployment/terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:

```hcl
tenancy_ocid     = "ocid1.tenancy.oc1..xxxxx"  # From Step 3B
user_ocid        = "ocid1.user.oc1..xxxxx"     # From Step 3A
fingerprint      = "12:34:56:78:90:ab:cd:ef..." # From Step 2
private_key_path = "~/.oci/oci_api_key.pem"
region           = "us-ashburn-1"              # Your region
compartment_ocid = "ocid1.compartment.oc1..xxxxx" # From Step 6 (or use tenancy_ocid)
```

## Troubleshooting

### Error: "InvalidKeyPair"
- Check that the public key in OCI Console matches your `oci_api_key_public.pem`
- Make sure you copied the ENTIRE key (including BEGIN/END lines)

### Error: "NotAuthenticated"
- Verify fingerprint matches exactly
- Check private key path is correct
- Ensure you're using the right region

### Error: "AuthorizationFailed"
- Your user might not have permissions
- Contact your administrator or check IAM policies

## Quick Reference Commands

```bash
# View public key
cat ~/.oci/oci_api_key_public.pem

# View config
cat ~/.oci/config

# Test connection
oci iam region list

# List compartments
oci iam compartment list

# Get current user info
oci iam user get --user-id $(oci iam user list --query 'data[0].id' --raw-output)
```

## Next Steps

Once OCI CLI is configured:

1. ✅ Test: `oci iam region list` should work
2. ✅ Update `terraform.tfvars` with your values
3. ✅ Read `deployment/README.md` to continue
4. ✅ Start with Docker (build images locally first)

You're ready to deploy! 🚀

