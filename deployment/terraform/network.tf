# ============================================
# STEP 3.3: Networking (VCN, Subnets, etc.)
# ============================================
# This creates the network infrastructure

# Virtual Cloud Network (VCN)
# Think of it as your private network in the cloud
resource "oci_core_vcn" "main" {
  compartment_id = var.compartment_ocid
  cidr_blocks    = ["10.0.0.0/16"]  # Private IP range
  display_name   = "${var.project_name}-vcn"
  dns_label      = var.project_name
  
  # Enable DNS resolution
  is_ipv6enabled = false
}

# Internet Gateway
# Allows resources in public subnet to access internet
resource "oci_core_internet_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-igw"
  enabled        = true
}

# NAT Gateway
# Allows resources in private subnet to access internet
# (for downloading Docker images, etc.)
resource "oci_core_nat_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-nat"
}

# Service Gateway
# For accessing OCI services (Object Storage, etc.)
resource "oci_core_service_gateway" "main" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-sgw"
  
  services {
    service_id = data.oci_core_services.all_services.services[0].id
  }
}

# Route Table for Public Subnet
# Routes traffic to Internet Gateway
resource "oci_core_route_table" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-public-rt"
  
  route_rules {
    network_entity_id = oci_core_internet_gateway.main.id
    destination       = "0.0.0.0/0"  # All traffic
    destination_type  = "CIDR_BLOCK"
  }
}

# Route Table for Private Subnet
# Routes traffic to NAT Gateway (outbound) and Service Gateway
resource "oci_core_route_table" "private" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-private-rt"
  
  route_rules {
    network_entity_id = oci_core_nat_gateway.main.id
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
  }
  
  route_rules {
    network_entity_id = oci_core_service_gateway.main.id
    destination       = data.oci_core_services.all_services.services[0].cidr_block
    destination_type  = "SERVICE_CIDR_BLOCK"
  }
}

# Security List for Public Subnet
# Firewall rules - what traffic is allowed
resource "oci_core_security_list" "public" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-public-sl"
  
  # Allow inbound HTTP
  ingress_security_rules {
    protocol    = "6"  # TCP
    source      = "0.0.0.0/0"
    source_type = "CIDR_BLOCK"
    
    tcp_options {
      min = 80
      max = 80
    }
  }
  
  # Allow inbound HTTPS
  ingress_security_rules {
    protocol    = "6"
    source      = "0.0.0.0/0"
    source_type = "CIDR_BLOCK"
    
    tcp_options {
      min = 443
      max = 443
    }
  }
  
  # Allow all outbound
  egress_security_rules {
    protocol         = "all"
    destination      = "0.0.0.0/0"
    destination_type = "CIDR_BLOCK"
  }
}

# Security List for Private Subnet
resource "oci_core_security_list" "private" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.main.id
  display_name   = "${var.project_name}-private-sl"
  
  # Allow all traffic from VCN
  ingress_security_rules {
    protocol    = "all"
    source      = "10.0.0.0/16"
    source_type = "CIDR_BLOCK"
  }
  
  # Allow all outbound
  egress_security_rules {
    protocol         = "all"
    destination      = "0.0.0.0/0"
    destination_type = "CIDR_BLOCK"
  }
}

# Public Subnet
# For load balancers, bastion hosts
resource "oci_core_subnet" "public" {
  compartment_id    = var.compartment_ocid
  vcn_id            = oci_core_vcn.main.id
  cidr_block        = "10.0.1.0/24"
  display_name      = "${var.project_name}-public-subnet"
  dns_label         = "public"
  security_list_ids = [oci_core_security_list.public.id]
  route_table_id    = oci_core_route_table.public.id
}

# Private Subnet
# For Kubernetes worker nodes (more secure)
resource "oci_core_subnet" "private" {
  compartment_id    = var.compartment_ocid
  vcn_id            = oci_core_vcn.main.id
  cidr_block        = "10.0.2.0/24"
  display_name      = "${var.project_name}-private-subnet"
  dns_label         = "private"
  security_list_ids = [oci_core_security_list.private.id]
  route_table_id    = oci_core_route_table.private.id
  prohibit_public_ip_on_vnic = true  # No public IPs
}

# Data source for OCI services
data "oci_core_services" "all_services" {
  filter {
    name   = "name"
    values = ["All .* Services In Oracle Services Network"]
    regex  = true
  }
}

