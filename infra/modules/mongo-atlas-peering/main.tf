data "aws_caller_identity" "current" {}

# 1) Network container in Atlas
resource "mongodbatlas_network_container" "container" {
  project_id       = var.mongodb_project_id
  atlas_cidr_block = var.atlas_cidr_block
  provider_name    = "AWS"
}

# 2) Create peering from Atlas to your AWS VPC
resource "mongodbatlas_network_peering" "peering" {
  project_id           = var.mongodb_project_id
  container_id         = mongodbatlas_network_container.container.container_id

  provider_name        = "AWS"
  aws_account_id       = data.aws_caller_identity.current.account_id
  vpc_id               = var.aws_vpc_id
  route_table_cidr_block = var.aws_vpc_cidr
  accepter_region_name = var.aws_region
}

# 3) Accept paring since aws
resource "aws_vpc_peering_connection_accepter" "this" {
  vpc_peering_connection_id = mongodbatlas_network_peering.peering.vpc_peering_connection_id
  auto_accept               = true

  tags = {
    Name = "${var.project_name}-atlas-peering"
  }
}

# 4) Routes from your private route tables to the Atlas CIDR
resource "aws_route" "to_atlas" {
  for_each = toset(var.aws_route_table_ids)

  route_table_id            = each.value
  destination_cidr_block    = var.atlas_cidr_block
  vpc_peering_connection_id = aws_vpc_peering_connection_accepter.this.id
}

# 5) Whitelist of your VPC in Atlas
resource "mongodbatlas_project_ip_access_list" "vpc_whitelist" {
  project_id = var.mongodb_project_id
  cidr_block = var.aws_vpc_cidr
  comment    = "AWS VPC network for voting system"
}
