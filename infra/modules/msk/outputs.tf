output "bootstrap_brokers" {
  description = "Bootstrap broker string for clients"
  value       = aws_msk_cluster.this.bootstrap_brokers
}
