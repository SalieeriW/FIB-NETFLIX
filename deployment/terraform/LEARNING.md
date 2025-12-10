# Learning: Terraform Concepts

## Key Concepts

### 1. **Resources**
Resources are the things you create:
```hcl
resource "oci_core_vcn" "main" {
  # Configuration
}
```
- `oci_core_vcn` = resource type
- `main` = local name (reference it elsewhere)
- Block contains configuration

### 2. **Variables**
Make your config reusable:
```hcl
variable "region" {
  type = string
  default = "us-ashburn-1"
}
```
Use: `var.region`

### 3. **Data Sources**
Get information about existing resources:
```hcl
data "oci_identity_availability_domains" "ads" {
  compartment_id = var.tenancy_ocid
}
```
Use: `data.oci_identity_availability_domains.ads`

### 4. **Outputs**
Values you want to see after apply:
```hcl
output "cluster_id" {
  value = oci_containerengine_cluster.main.id
}
```

### 5. **Dependencies**
Terraform automatically figures out dependencies:
- Subnet needs VCN → creates VCN first
- Node pool needs cluster → creates cluster first

## Terraform Workflow

```bash
# 1. Initialize (download providers)
terraform init

# 2. Format code
terraform fmt

# 3. Validate syntax
terraform validate

# 4. Plan (see what will change)
terraform plan

# 5. Apply (create infrastructure)
terraform apply

# 6. Destroy (tear down)
terraform destroy
```

## OCI Concepts

### OCID (Oracle Cloud Identifier)
Every resource has a unique OCID:
- `ocid1.tenancy.oc1..aaaaaaa...`
- Like AWS ARN

### Compartment
Logical container for organizing resources:
- Like folders
- Can nest compartments

### VCN (Virtual Cloud Network)
Your private network:
- Like AWS VPC
- Has subnets, route tables, security lists

### Shape
Instance type (CPU, memory):
- `VM.Standard.A1.Flex` = ARM-based, flexible
- Free tier: 4 OCPUs, 24GB RAM total

## Next: Prometheus & Grafana

