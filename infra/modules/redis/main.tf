resource "aws_security_group" "redis_sg" {
  name        = "${var.project_name}-redis-sg"
  description = "Redis security group"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-redis-sg"
  }
}

resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.project_name}-redis-subnets"
  subnet_ids = var.subnets

  tags = {
    Name = "${var.project_name}-redis-subnets"
  }
}

resource "aws_elasticache_replication_group" "this" {
  replication_group_id          = "${var.project_name}-redis-rg"
  description                   = "Redis for voting results"
  engine                        = "redis"
  engine_version                = "7.0"
  node_type                     = "cache.t3.micro"
  number_cache_clusters         = 1
  parameter_group_name          = "default.redis7"
  automatic_failover_enabled    = false
  transit_encryption_enabled    = false
  at_rest_encryption_enabled    = false
  subnet_group_name             = aws_elasticache_subnet_group.this.name
  security_group_ids            = [aws_security_group.redis_sg.id]

  tags = {
    Name = "${var.project_name}-redis"
  }
}
