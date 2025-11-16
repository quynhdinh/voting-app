resource "mongodbatlas_cluster" "cluster" {
  project_id = var.project_id
  name       = "${var.project_name}-mongo"

  cluster_type = "REPLICASET"

  provider_name               = "AWS"
  backing_provider_name       = "AWS"
  provider_region_name        = var.aws_region
  provider_instance_size_name = "M10"
  provider_backup_enabled     = false
  auto_scaling_disk_gb_enabled = true
}

# -----------------------------
# Create VPC peering in Atlas
# -----------------------------
resource "mongodbatlas_network_peering" "peering" {
  accepter_region_name = var.aws_region
  project_id           = var.project_id
  container_id         = mongodbatlas_network_container.container.container_id

  provider_name = "AWS"
  route_table_cidr_block = var.aws_vpc_cidr

  vpc_id     = var.aws_vpc_id
  aws_account_id = data.aws_caller_identity.current.account_id
}

resource "mongodbatlas_network_container" "container" {
  project_id    = var.project_id
  atlas_cidr_block = "192.168.0.0/21"
  provider_name = "AWS"
}

data "aws_caller_identity" "current" {}

# -----------------------------
# Whitelist your VPC CIDR
# -----------------------------
resource "mongodbatlas_project_ip_access_list" "vpc_whitelist" {
  project_id = var.project_id
  cidr_block = var.aws_vpc_cidr
  comment    = "AWS VPC network"
}
