output "atlas_cidr_block" {
  value = var.atlas_cidr_block
}

output "peering_connection_id" {
  value = aws_vpc_peering_connection_accepter.this.id
}

output "peering_status" {
  value = mongodbatlas_network_peering.peering.connection_status
}
