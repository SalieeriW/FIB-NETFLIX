# ============================================
# STEP 3.5: Outputs
# ============================================
# Outputs are values you want to see after Terraform runs

output "vcn_id" {
  value       = oci_core_vcn.main.id
  description = "VCN OCID"
}

output "cluster_id" {
  value       = oci_containerengine_cluster.main.id
  description = "OKE Cluster OCID"
}

output "public_subnet_id" {
  value       = oci_core_subnet.public.id
  description = "Public Subnet OCID"
}

output "private_subnet_id" {
  value       = oci_core_subnet.private.id
  description = "Private Subnet OCID"
}

# Instructions for getting kubeconfig
output "kubeconfig_instructions" {
  value = <<-EOT
    To get kubeconfig, run:
    oci ce cluster create-kubeconfig \
      --cluster-id ${oci_containerengine_cluster.main.id} \
      --file $HOME/.kube/config \
      --region ${var.region} \
      --token-version 2.0.0
  EOT
}

