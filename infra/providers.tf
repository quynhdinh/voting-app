terraform {
  required_version = ">= 1.6.0"
  mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 1.20"
  }
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

provider "mongodbatlas" {
  public_key  = var.mongodb_public_key
  private_key = var.mongodb_private_key
}