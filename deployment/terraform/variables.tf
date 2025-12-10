# ============================================
# STEP 3.2: Variables
# ============================================
# Variables make Terraform configurable
# You can override these when running terraform apply

variable "tenancy_ocid" {
  description = "OCID of your tenancy"
  type        = string
}

variable "user_ocid" {
  description = "OCID of the user"
  type        = string
}

variable "fingerprint" {
  description = "Fingerprint of the API key"
  type        = string
}

variable "private_key_path" {
  description = "Path to private key file"
  type        = string
  default     = "~/.oci/oci_api_key.pem"
}

variable "region" {
  description = "OCI region"
  type        = string
  default     = "us-ashburn-1"  # Change to your preferred region
}

variable "compartment_ocid" {
  description = "OCID of compartment to create resources in"
  type        = string
}

variable "project_name" {
  description = "Name prefix for all resources"
  type        = string
  default     = "vidstream"
}

variable "kubernetes_version" {
  description = "Kubernetes version for OKE"
  type        = string
  default     = "v1.28.2"
}

variable "node_pool_size" {
  description = "Number of worker nodes"
  type        = number
  default     = 2
}

variable "node_shape" {
  description = "Shape (instance type) for worker nodes"
  type        = string
  default     = "VM.Standard.A1.Flex"  # ARM-based, free tier eligible
}

variable "node_ocpus" {
  description = "Number of OCPUs per node"
  type        = number
  default     = 2  # Free tier: 4 OCPUs total, so 2 nodes × 2 OCPUs
}

variable "node_memory_gb" {
  description = "Memory in GB per node"
  type        = number
  default     = 12  # Free tier: 24GB total, so 2 nodes × 12GB
}

