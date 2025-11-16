variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnets" {
  type = list(string)
}

variable "db_username" {
  type        = string
  default     = "appuser"
}

variable "db_password" {
  type        = string
  sensitive   = true
  default     = "changeme123!"  # en real: usa tfvars o SSM
}

variable "db_name" {
  type        = string
  default     = "votingdb"
}
