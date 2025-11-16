module "vpc" {
  source       = "./modules/vpc"
  project_name = var.project_name
  aws_region   = var.aws_region
}

module "rds" {
  source       = "./modules/rds"
  project_name = var.project_name
  vpc_id       = module.vpc.vpc_id
  subnets      = module.vpc.private_subnets
}

module "redis" {
  source       = "./modules/redis"
  project_name = var.project_name
  vpc_id       = module.vpc.vpc_id
  subnets      = module.vpc.private_subnets
}

module "msk" {
  source       = "./modules/msk"
  project_name = var.project_name
  vpc_id       = module.vpc.vpc_id
  subnets      = module.vpc.private_subnets
}

module "ecs_cluster" {
  source       = "./modules/ecs-cluster"
  project_name = var.project_name
  vpc_id       = module.vpc.vpc_id
  subnets      = module.vpc.private_subnets
}

# Reusing module ecs-service for each microservice

module "user_service" {
  source           = "./modules/ecs-service"
  project_name     = var.project_name
  service_name     = "user-service"
  cluster_arn      = module.ecs_cluster.cluster_arn
  alb_listener_arn = module.ecs_cluster.alb_listener_arn
  subnets          = module.vpc.private_subnets
  security_groups  = [module.vpc.app_sg_id]
  container_image  = "your-docker-registry/user-service:latest"
  container_port   = 8080
  desired_count    = 2
}

module "contest_service" {
  source           = "./modules/ecs-service"
  project_name     = var.project_name
  service_name     = "contest-service"
  cluster_arn      = module.ecs_cluster.cluster_arn
  alb_listener_arn = module.ecs_cluster.alb_listener_arn
  subnets          = module.vpc.private_subnets
  security_groups  = [module.vpc.app_sg_id]
  container_image  = "your-docker-registry/contest-service:latest"
  container_port   = 8080
  desired_count    = 2
}

module "voting_service" {
  source           = "./modules/ecs-service"
  project_name     = var.project_name
  service_name     = "voting-service"
  cluster_arn      = module.ecs_cluster.cluster_arn
  alb_listener_arn = module.ecs_cluster.alb_listener_arn
  subnets          = module.vpc.private_subnets
  security_groups  = [module.vpc.app_sg_id]
  container_image  = "your-docker-registry/voting-service:latest"
  container_port   = 8080
  desired_count    = 3

  env_vars = {
    KAFKA_BOOTSTRAP_SERVERS = module.msk.bootstrap_brokers
    # MONGO_URI               = "mongodb+srv://user:pass@your-atlas-cluster/voting" # real: variable or secret
     MONGO_URI               = module.mongo_atlas.connection_string
  }
}

module "results_service" {
  source           = "./modules/ecs-service"
  project_name     = var.project_name
  service_name     = "results-service"
  cluster_arn      = module.ecs_cluster.cluster_arn
  alb_listener_arn = module.ecs_cluster.alb_listener_arn
  subnets          = module.vpc.private_subnets
  security_groups  = [module.vpc.app_sg_id]
  container_image  = "your-docker-registry/results-service:latest"
  container_port   = 8080
  desired_count    = 2

  env_vars = {
    KAFKA_BOOTSTRAP_SERVERS = module.msk.bootstrap_brokers
    REDIS_HOST              = module.redis.redis_primary_endpoint
    REDIS_PORT              = tostring(module.redis.redis_port)
    RESULTS_DB_HOST         = module.rds.db_endpoint
    RESULTS_DB_PORT         = tostring(module.rds.db_port)
    RESULTS_DB_NAME         = module.rds.db_name
    RESULTS_DB_USER         = module.rds.db_username
    RESULTS_DB_PASSWORD     = "changeme123!"  # mejor desde SSM/Secrets Manager
  }
}

module "mongo_atlas" {
  source          = "./modules/mongo-atlas"
  project_name    = var.project_name
  project_id      = var.mongodb_project_id
  aws_vpc_id      = module.vpc.vpc_id
  aws_region      = var.aws_region
  aws_vpc_cidr    = "10.0.0.0/16"
}

