variable "project_name"    { type = string }
variable "service_name"    { type = string }
variable "cluster_arn"     { type = string }
variable "alb_listener_arn"{ type = string }
variable "subnets"         { type = list(string) }
variable "security_groups" { type = list(string) }

variable "container_image" {
  type = string
}

variable "container_port" {
  type    = number
  default = 8080
}

variable "desired_count" {
  type    = number
  default = 2
}

variable "env_vars" {
  description = "Environment variables for the container"
  type        = map(string)
  default     = {}
}