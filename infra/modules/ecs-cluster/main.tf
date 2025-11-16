variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnets" {
  type = list(string)
}

resource "aws_ecs_cluster" "this" {
  name = "${var.project_name}-cluster"
}

# ALB en subnets públicas normalmente, pero aquí asumimos `subnets` son públicas
resource "aws_lb" "app_alb" {
  name               = "${var.project_name}-alb"
  load_balancer_type = "application"
  subnets            = var.subnets

  tags = {
    Name = "${var.project_name}-alb"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.app_alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

output "cluster_arn" {
  value = aws_ecs_cluster.this.arn
}

output "alb_listener_arn" {
  value = aws_lb_listener.http.arn
}

output "alb_arn" {
  value = aws_lb.app_alb.arn
}
