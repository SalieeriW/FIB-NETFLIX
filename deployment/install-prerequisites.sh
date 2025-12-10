#!/bin/bash
# ============================================
# VidStream Prerequisites Installation Script
# ============================================
# This script installs all required tools for deployment

set -e  # Exit on error

echo "🚀 Installing VidStream Deployment Prerequisites..."
echo ""

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
    echo "📱 Detected: macOS"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
    echo "🐧 Detected: Linux"
else
    echo "❌ Unsupported OS: $OSTYPE"
    echo "Please install manually (see INSTALL.md)"
    exit 1
fi

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to install Docker
install_docker() {
    echo ""
    echo "📦 Installing Docker..."
    if command_exists docker; then
        echo "✅ Docker already installed: $(docker --version)"
        return
    fi
    
    if [ "$OS" == "macos" ]; then
        if command_exists brew; then
            echo "Installing via Homebrew..."
            brew install --cask docker
            echo "✅ Docker Desktop installed. Please start Docker Desktop from Applications."
        else
            echo "❌ Homebrew not found. Please install Docker Desktop manually:"
            echo "   https://www.docker.com/products/docker-desktop"
        fi
    else
        echo "Installing via official script..."
        curl -fsSL https://get.docker.com | sh
        sudo usermod -aG docker $USER
        echo "✅ Docker installed. You may need to log out and back in."
    fi
}

# Function to install kubectl
install_kubectl() {
    echo ""
    echo "📦 Installing kubectl..."
    if command_exists kubectl; then
        echo "✅ kubectl already installed: $(kubectl version --client --short 2>/dev/null || echo 'installed')"
        return
    fi
    
    if [ "$OS" == "macos" ]; then
        if command_exists brew; then
            brew install kubectl
        else
            echo "Downloading kubectl..."
            KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)
            curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/darwin/amd64/kubectl"
            chmod +x kubectl
            sudo mv kubectl /usr/local/bin/
        fi
    else
        echo "Downloading kubectl..."
        KUBECTL_VERSION=$(curl -L -s https://dl.k8s.io/release/stable.txt)
        curl -LO "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"
        chmod +x kubectl
        sudo mv kubectl /usr/local/bin/
    fi
    echo "✅ kubectl installed"
}

# Function to install Terraform
install_terraform() {
    echo ""
    echo "📦 Installing Terraform..."
    if command_exists terraform; then
        echo "✅ Terraform already installed: $(terraform version | head -n1)"
        return
    fi
    
    if [ "$OS" == "macos" ]; then
        if command_exists brew; then
            brew tap hashicorp/tap
            brew install hashicorp/tap/terraform
        else
            echo "Downloading Terraform..."
            TERRAFORM_VERSION="1.6.0"
            wget -q "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_darwin_amd64.zip"
            unzip -q "terraform_${TERRAFORM_VERSION}_darwin_amd64.zip"
            sudo mv terraform /usr/local/bin/
            rm "terraform_${TERRAFORM_VERSION}_darwin_amd64.zip"
        fi
    else
        echo "Downloading Terraform..."
        TERRAFORM_VERSION="1.6.0"
        wget -q "https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip"
        unzip -q "terraform_${TERRAFORM_VERSION}_linux_amd64.zip"
        sudo mv terraform /usr/local/bin/
        rm "terraform_${TERRAFORM_VERSION}_linux_amd64.zip"
    fi
    echo "✅ Terraform installed"
}

# Function to install OCI CLI
install_oci_cli() {
    echo ""
    echo "📦 Installing OCI CLI..."
    if command_exists oci; then
        echo "✅ OCI CLI already installed: $(oci --version)"
        return
    fi
    
    if [ "$OS" == "macos" ]; then
        if command_exists brew; then
            brew install oci-cli
        else
            echo "Installing via install script..."
            bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)" <<EOF

~/lib/oracle-cli
y
EOF
            echo 'export PATH="$HOME/lib/oracle-cli/bin:$PATH"' >> ~/.zshrc
            export PATH="$HOME/lib/oracle-cli/bin:$PATH"
        fi
    else
        echo "Installing via install script..."
        bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)" <<EOF

~/lib/oracle-cli
y
EOF
        echo 'export PATH="$HOME/lib/oracle-cli/bin:$PATH"' >> ~/.bashrc
        export PATH="$HOME/lib/oracle-cli/bin:$PATH"
    fi
    echo "✅ OCI CLI installed"
}

# Main installation
echo "Starting installation..."
echo ""

install_docker
install_kubectl
install_terraform
install_oci_cli

# Verification
echo ""
echo "════════════════════════════════════════"
echo "✅ Installation Complete!"
echo "════════════════════════════════════════"
echo ""
echo "Verification:"
echo ""

if command_exists docker; then
    echo "✅ Docker: $(docker --version 2>/dev/null | head -n1)"
else
    echo "❌ Docker: Not found"
fi

if command_exists kubectl; then
    echo "✅ kubectl: $(kubectl version --client --short 2>/dev/null || echo 'installed')"
else
    echo "❌ kubectl: Not found"
fi

if command_exists terraform; then
    echo "✅ Terraform: $(terraform version | head -n1)"
else
    echo "❌ Terraform: Not found"
fi

if command_exists oci; then
    echo "✅ OCI CLI: $(oci --version 2>/dev/null || echo 'installed')"
else
    echo "❌ OCI CLI: Not found"
fi

echo ""
echo "════════════════════════════════════════"
echo "📋 Next Steps:"
echo "════════════════════════════════════════"
echo ""
echo "1. Set up Oracle Cloud account:"
echo "   https://cloud.oracle.com"
echo ""
echo "2. Configure OCI CLI:"
echo "   oci setup config"
echo ""
echo "3. Generate API keys:"
echo "   mkdir -p ~/.oci"
echo "   openssl genrsa -out ~/.oci/oci_api_key.pem 2048"
echo "   openssl rsa -pubout -in ~/.oci/oci_api_key.pem -out ~/.oci/oci_api_key_public.pem"
echo "   # Upload public key to OCI Console → User Settings → API Keys"
echo ""
echo "4. Get your OCIDs from OCI Console:"
echo "   - User OCID: User Settings → User Information"
echo "   - Tenancy OCID: Administration → Tenancy Details"
echo ""
echo "5. Read deployment/INSTALL.md for detailed instructions"
echo ""
echo "6. Start with: deployment/README.md"
echo ""

