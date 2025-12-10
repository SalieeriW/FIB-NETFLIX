# Installation Guide - Prerequisites

This guide helps you install all required tools for the deployment.

## Prerequisites Checklist

- [ ] Oracle Cloud account (free tier)
- [ ] Docker installed locally
- [ ] kubectl installed
- [ ] Terraform installed
- [ ] OCI CLI configured
- [ ] Basic understanding of YAML

---

## 1. Oracle Cloud Account (Free Tier)

### Steps:
1. Go to https://cloud.oracle.com
2. Click "Start for Free"
3. Fill in your details
4. Verify email
5. Complete account setup

### What You Get:
- 2 AMD VMs OR 4 ARM VMs (Ampere A1)
- 200GB block storage
- 10TB egress/month
- 2 Autonomous Databases

### After Signup:
- Note your **Tenancy OCID** (found in Account Settings)
- Note your **User OCID** (found in User Settings)
- You'll need these for Terraform

---

## 2. Docker Installation

### macOS
```bash
# Option 1: Using Homebrew (recommended)
brew install --cask docker

# Option 2: Download Docker Desktop
# Visit: https://www.docker.com/products/docker-desktop
# Download and install Docker Desktop for Mac

# Verify installation
docker --version
docker run hello-world
```

### Linux (Ubuntu/Debian)
```bash
# Remove old versions
sudo apt-get remove docker docker-engine docker.io containerd runc

# Update package index
sudo apt-get update

# Install prerequisites
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add your user to docker group (to run without sudo)
sudo usermod -aG docker $USER

# Log out and back in, then verify
docker --version
docker run hello-world
```

### Windows
1. Download Docker Desktop: https://www.docker.com/products/docker-desktop
2. Install and restart
3. Verify:
```powershell
docker --version
docker run hello-world
```

---

## 3. kubectl Installation

### macOS
```bash
# Using Homebrew (easiest)
brew install kubectl

# Or download directly
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Verify
kubectl version --client
```

### Linux
```bash
# Download latest stable
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Make executable
chmod +x kubectl

# Move to PATH
sudo mv kubectl /usr/local/bin/

# Verify
kubectl version --client
```

### Windows
```powershell
# Using Chocolatey
choco install kubernetes-cli

# Or download manually
# Visit: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
# Download kubectl.exe and add to PATH

# Verify
kubectl version --client
```

---

## 4. Terraform Installation

### macOS
```bash
# Using Homebrew (recommended)
brew tap hashicorp/tap
brew install hashicorp/tap/terraform

# Or download manually
# Visit: https://www.terraform.io/downloads
# Download for macOS, unzip, move to /usr/local/bin

# Verify
terraform version
```

### Linux
```bash
# Download latest version
wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip

# Unzip
unzip terraform_1.6.0_linux_amd64.zip

# Move to PATH
sudo mv terraform /usr/local/bin/

# Verify
terraform version
```

### Windows
```powershell
# Using Chocolatey
choco install terraform

# Or download manually
# Visit: https://www.terraform.io/downloads
# Download for Windows, unzip, add to PATH

# Verify
terraform version
```

---

## 5. OCI CLI Installation

### macOS
```bash
# Using Homebrew
brew install oci-cli

# Or install script
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Verify
oci --version
```

### Linux
```bash
# Install script (recommended)
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Follow prompts:
# - Installation location: Press Enter (default: ~/lib/oracle-cli)
# - Add to PATH: Yes

# Reload shell or:
source ~/.bashrc

# Verify
oci --version
```

### Windows
```powershell
# Using pip (Python required)
pip install oci-cli

# Verify
oci --version
```

### OCI CLI Configuration

After installation, configure OCI CLI:

```bash
# Run configuration wizard
oci setup config

# You'll need:
# 1. User OCID (from OCI Console → User Settings)
# 2. Tenancy OCID (from OCI Console → Administration → Tenancy Details)
# 3. Region (e.g., us-ashburn-1, us-phoenix-1)
# 4. Generate API key pair (if you haven't)

# Generate API key (if needed)
mkdir -p ~/.oci
openssl genrsa -out ~/.oci/oci_api_key.pem 2048
openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem

# Upload public key to OCI:
# 1. Go to OCI Console → User Settings → API Keys
# 2. Click "Add API Key"
# 3. Paste content of ~/.oci/oci_api_key_public.pem
# 4. Note the fingerprint shown

# Test configuration
oci iam region list
```

### API Key Setup (Detailed)

1. **Generate Key Pair:**
   ```bash
   mkdir -p ~/.oci
   openssl genrsa -out ~/.oci/oci_api_key.pem 2048
   chmod 600 ~/.oci/oci_api_key.pem
   openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem
   ```

2. **Upload Public Key:**
   - Go to OCI Console
   - User Settings → API Keys → Add API Key
   - Choose "Paste Public Key"
   - Copy content of `~/.oci/oci_api_key_public.pem`
   - Paste and click "Add"
   - **Copy the fingerprint** (you'll need it)

3. **Get OCIDs:**
   - **User OCID**: User Settings → User Information
   - **Tenancy OCID**: Administration → Tenancy Details

4. **Configure:**
   ```bash
   oci setup config
   # Enter:
   # - User OCID
   # - Tenancy OCID
   # - Region
   # - Path to private key: ~/.oci/oci_api_key.pem
   # - Fingerprint (from step 2)
   ```

---

## 6. YAML Basics

YAML is used for Kubernetes manifests and configuration files.

### Quick Reference:

```yaml
# Comments start with #

# Key-value pairs
name: vidstream
version: 1.0

# Lists (arrays)
ports:
  - 8080
  - 9090

# Nested objects
metadata:
  name: my-app
  labels:
    app: python-service

# Multi-line strings
description: |
  This is a multi-line
  string that preserves
  line breaks

# Folded strings (single line)
description: >
  This is a multi-line
  string that becomes
  a single line
```

### Resources to Learn:
- **Interactive**: https://www.yaml.org/start.html
- **Tutorial**: https://learnxinyminutes.com/docs/yaml/
- **Practice**: Edit the Kubernetes YAML files in this project

### Common Mistakes:
- **Indentation**: Use spaces, not tabs (2 spaces standard)
- **Colons**: Need space after `:` (key: value, not key:value)
- **Lists**: Use `-` for list items
- **Quotes**: Usually optional, but use quotes for special characters

---

## Quick Install Script (Linux/macOS)

Save this as `install-prerequisites.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 Installing VidStream Deployment Prerequisites..."

# Check OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
else
    echo "❌ Unsupported OS: $OSTYPE"
    exit 1
fi

# Install Docker
echo "📦 Installing Docker..."
if command -v docker &> /dev/null; then
    echo "✅ Docker already installed"
else
    if [ "$OS" == "macos" ]; then
        brew install --cask docker
    else
        curl -fsSL https://get.docker.com | sh
        sudo usermod -aG docker $USER
    fi
fi

# Install kubectl
echo "📦 Installing kubectl..."
if command -v kubectl &> /dev/null; then
    echo "✅ kubectl already installed"
else
    if [ "$OS" == "macos" ]; then
        brew install kubectl
    else
        curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
        chmod +x kubectl
        sudo mv kubectl /usr/local/bin/
    fi
fi

# Install Terraform
echo "📦 Installing Terraform..."
if command -v terraform &> /dev/null; then
    echo "✅ Terraform already installed"
else
    if [ "$OS" == "macos" ]; then
        brew tap hashicorp/tap
        brew install hashicorp/tap/terraform
    else
        wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
        unzip terraform_1.6.0_linux_amd64.zip
        sudo mv terraform /usr/local/bin/
        rm terraform_1.6.0_linux_amd64.zip
    fi
fi

# Install OCI CLI
echo "📦 Installing OCI CLI..."
if command -v oci &> /dev/null; then
    echo "✅ OCI CLI already installed"
else
    if [ "$OS" == "macos" ]; then
        brew install oci-cli
    else
        bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
    fi
fi

# Verify installations
echo ""
echo "✅ Verification:"
echo "Docker: $(docker --version 2>/dev/null || echo 'Not found')"
echo "kubectl: $(kubectl version --client 2>/dev/null | head -n1 || echo 'Not found')"
echo "Terraform: $(terraform version 2>/dev/null | head -n1 || echo 'Not found')"
echo "OCI CLI: $(oci --version 2>/dev/null || echo 'Not found')"

echo ""
echo "🎉 Installation complete!"
echo ""
echo "Next steps:"
echo "1. Set up Oracle Cloud account: https://cloud.oracle.com"
echo "2. Configure OCI CLI: oci setup config"
echo "3. Generate API keys and get OCIDs from OCI Console"
echo "4. Review deployment/README.md to continue"
```

Make it executable:
```bash
chmod +x install-prerequisites.sh
./install-prerequisites.sh
```

---

## Verification Checklist

After installation, verify everything works:

```bash
# Docker
docker --version
docker run hello-world

# kubectl
kubectl version --client

# Terraform
terraform version

# OCI CLI
oci --version
oci iam region list  # Should list regions (after config)
```

---

## Troubleshooting

### Docker Issues
- **Permission denied**: Add user to docker group: `sudo usermod -aG docker $USER` (then log out/in)
- **Docker daemon not running**: Start Docker Desktop (macOS/Windows) or `sudo systemctl start docker` (Linux)

### kubectl Issues
- **Command not found**: Check PATH, ensure binary is in `/usr/local/bin` or similar
- **Version mismatch**: Download matching version for your OS architecture

### Terraform Issues
- **Provider errors**: Run `terraform init` in your terraform directory
- **Version too old**: Download latest from hashicorp.com

### OCI CLI Issues
- **Config not found**: Run `oci setup config`
- **Authentication failed**: Check API key fingerprint matches
- **Region not found**: Use correct region identifier (e.g., `us-ashburn-1`)

---

## Next Steps

Once all tools are installed:

1. ✅ Set up Oracle Cloud account
2. ✅ Configure OCI CLI (`oci setup config`)
3. ✅ Get your OCIDs (User, Tenancy, Compartment)
4. ✅ Generate API keys
5. ✅ Read `deployment/README.md`
6. ✅ Start with Docker (build images locally)
7. ✅ Test with Docker Compose
8. ✅ Deploy to Kubernetes

Happy deploying! 🚀

