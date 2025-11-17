variable "project_name" {
  type = string
}

variable "mongodb_project_id" {
  description = "MongoDB Atlas Project ID"
  type        = string
}

variable "aws_vpc_id" {
  description = "AWS VPC ID donde corren tus microservicios"
  type        = string
}

variable "aws_region" {
  description = "Región AWS (ej: us-east-1)"
  type        = string
}

variable "aws_vpc_cidr" {
  description = "CIDR de la VPC de AWS (ej: 10.0.0.0/16)"
  type        = string
}

variable "aws_route_table_ids" {
  description = "Lista de route tables privadas donde agregar rutas hacia Atlas"
  type        = list(string)
}

variable "atlas_cidr_block" {
  description = "CIDR que usará Atlas para su red privada (no debe colisionar con tu VPC)"
  type        = string
  default     = "192.168.0.0/21"
}
