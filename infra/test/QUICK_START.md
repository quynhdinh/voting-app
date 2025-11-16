# Quick Start Guide

## Installation (One-time Setup)

```bash
# 1. Install Go (if not already installed)
brew install go  # macOS
# or
sudo apt-get install golang-go  # Linux

# 2. Navigate to test directory
cd infra/test

# 3. Download dependencies
go mod download
```

## Running Tests

### Run All Tests
```bash
go test -v -timeout 90m
```

### Run Individual Tests

```bash
# VPC and Subnets (Case 1) - ~15 minutes
go test -v -timeout 30m -run TestVPCModule

# ECS Cluster (Case 2) - ~20 minutes
go test -v -timeout 30m -run TestECSClusterModule

# ECS Services (Case 3) - ~45 minutes
go test -v -timeout 60m -run TestECSServiceModule

# RDS Database (Case 4) - ~25 minutes
go test -v -timeout 30m -run TestRDSModule

# MSK Kafka (Case 5a) - ~35 minutes
go test -v -timeout 45m -run TestMSKModule

# Redis Cache (Case 5b) - ~20 minutes
go test -v -timeout 30m -run TestRedisModule
```

## Test Case Mapping

| Test Case | File | Function | Time |
|-----------|------|----------|------|
| 1. VPC & Subnets | `vpc_test.go` | `TestVPCModule()` | ~15m |
| 2. ECS Cluster & ALB | `ecs_cluster_test.go` | `TestECSClusterModule()` | ~20m |
| 3. ECS Services | `ecs_service_test.go` | `TestECSServiceModule()` | ~45m |
| 4. RDS Instance | `rds_test.go` | `TestRDSModule()` | ~25m |
| 5a. MSK Cluster | `msk_redis_test.go` | `TestMSKModule()` | ~35m |
| 5b. Redis Cache | `msk_redis_test.go` | `TestRedisModule()` | ~20m |

## Prerequisites Checklist

- [ ] Go 1.21+ installed
- [ ] AWS CLI configured (`aws configure`)
- [ ] AWS credentials with required permissions
- [ ] Terraform installed
- [ ] Dependencies downloaded (`go mod download`)

## Expected Output

### Success
```
=== RUN   TestVPCModule
--- PASS: TestVPCModule (15.23s)
PASS
ok      github.com/voting-app/infra/test    15.234s
```

### Failure
```
=== RUN   TestVPCModule
    vpc_test.go:32: VPC CIDR should be 10.0.0.0/16
        Expected: 10.0.0.0/16
        Actual:   10.0.1.0/16
--- FAIL: TestVPCModule (8.45s)
FAIL
```

## Troubleshooting

### "timeout exceeded"
Increase timeout: `go test -v -timeout 120m`

### "AWS credentials not found"
Run: `aws configure`

### "Terraform not found"
Install: `brew install terraform`

### "Resources still exist after test"
Manually destroy: 
```bash
cd ../modules/vpc
terraform destroy -auto-approve
```

## Important Notes

⚠️ **These tests create real AWS resources** - they will incur costs (estimated $5-10 per full run)

✅ **Resources are automatically cleaned up** - tests use `defer terraform.Destroy()`

🔒 **Tests are isolated** - each uses a unique project name

⏱️ **Tests are slow** - they create real infrastructure, not mocks

## More Information

- Full documentation: [README.md](./README.md)
- Detailed test coverage: [TEST_SUMMARY.md](./TEST_SUMMARY.md)
