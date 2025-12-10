# ============================================
# STEP 3.1: Terraform Main Configuration
# ============================================
# This file sets up the Terraform provider and basic config

terraform {
  required_version = ">= 1.0"
  
  # Backend: Where to store Terraform state
  # State = current state of infrastructure
  backend "s3" {
    # For OCI, we can use OCI Object Storage
    # This keeps state safe and allows team collaboration
    bucket   = "terraform-state"
    key      = "vidstream/terraform.tfstate"
    region   = "us-ashburn-1"
    # You'll need to configure OCI credentials
  }
  
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 5.0"
    }
  }
}

# Configure OCI Provider
# This tells Terraform how to connect to Oracle Cloud
provider "oci" {
  tenancy_ocid     = var.tenancy_ocid
  user_ocid        = var.user_ocid
  fingerprint      = var.fingerprint
  private_key_path = var.private_key_path
  region           = var.region
}

# Get availability domains
# ADs = data centers in a region (for redundancy)
data "oci_identity_availability_domains" "ads" {
  compartment_id = var.tenancy_ocid
}

# Get current user info
data "oci_identity_user" "current_user" {
  user_id = var.user_ocid
}

