# Step 3: Terraform - Infrastructure as Code

## What is Terraform?

Terraform is an **Infrastructure as Code** tool. Instead of clicking buttons in a web UI, you write code to define your infrastructure.

**Why Terraform?**
- **Reproducible**: Same code = same infrastructure every time
- **Version Controlled**: Track changes in Git
- **Collaborative**: Team can review infrastructure changes
- **Automated**: No manual clicking

## How Terraform Works

1. **Write** `.tf` files describing desired infrastructure
2. **Plan** (`terraform plan`) - see what will change
3. **Apply** (`terraform apply`) - create/update infrastructure
4. **Destroy** (`terraform destroy`) - tear down everything

## Oracle Cloud (OCI) Concepts

### Compartment
- Logical container for resources
- Like folders for organizing

### VCN (Virtual Cloud Network)
- Your private network in the cloud
- Like a virtual data center

### Subnet
- Subdivision of VCN
- Public subnet = internet accessible
- Private subnet = internal only

### Security List
- Firewall rules
- Controls what traffic is allowed

### OKE (Oracle Kubernetes Engine)
- Managed Kubernetes service
- OCI handles master nodes, you manage worker nodes

Let's create the infrastructure!

