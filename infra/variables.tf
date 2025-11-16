variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "CS590 Final Project"
  type        = string
  default     = "voting-system"
}

variable "mongodb_public_key" {
  type      = string
  sensitive = true
}

variable "mongodb_private_key" {
  type      = string
  sensitive = true
}

variable "mongodb_org_id" {
  type = string
}

variable "mongodb_project_id" {
  type = string
}