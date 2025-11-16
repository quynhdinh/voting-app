output "connection_string" {
  value = mongodbatlas_cluster.cluster.connection_strings[0].standard_srv
}

output "cluster_name" {
  value = mongodbatlas_cluster.cluster.name
}

output "peering_state" {
  value = mongodbatlas_network_peering.peering.connection_status
}
