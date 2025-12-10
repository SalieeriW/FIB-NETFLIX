# ============================================
# STEP 3.4: Kubernetes Cluster (OKE)
# ============================================
# This creates the managed Kubernetes cluster

# Cluster
resource "oci_containerengine_cluster" "main" {
  compartment_id     = var.compartment_ocid
  kubernetes_version = var.kubernetes_version
  name               = "${var.project_name}-cluster"
  vcn_id             = oci_core_vcn.main.id
  
  # Cluster options
  options {
    # Service LB (Load Balancer) subnet
    service_lb_subnet_ids = [oci_core_subnet.public.id]
    
    # Kubernetes dashboard
    add_ons {
      is_kubernetes_dashboard_enabled = true
      is_tiller_enabled               = false
    }
    
    # Admission controllers
    admission_controller_options {
      is_pod_security_policy_enabled = false
    }
  }
}

# Node Pool
# Worker nodes that run your containers
resource "oci_containerengine_node_pool" "main" {
  cluster_id         = oci_containerengine_cluster.main.id
  compartment_id     = var.compartment_ocid
  kubernetes_version  = var.kubernetes_version
  name                = "${var.project_name}-node-pool"
  node_shape          = var.node_shape
  
  # Node configuration
  node_config_details {
    size = var.node_pool_size
    
    placement_configs {
      availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
      subnet_id           = oci_core_subnet.private.id
    }
    
    # SSH key for accessing nodes (optional, for debugging)
    # key_ids = [oci_core_instance.main.id]
  }
  
  # Node shape config (for flexible shapes like A1.Flex)
  node_shape_config {
    memory_in_gbs = var.node_memory_gb
    ocpus         = var.node_ocpus
  }
  
  # Initial node labels
  initial_node_labels {
    key   = "workload"
    value = "vidstream"
  }
}

# Output: Kubeconfig
# This file lets kubectl connect to your cluster
output "kubeconfig" {
  value     = oci_containerengine_cluster.main.id
  sensitive = true
}

output "cluster_id" {
  value = oci_containerengine_cluster.main.id
}

