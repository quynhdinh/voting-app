# Terraform Infrastructure Unit Tests

This directory contains comprehensive unit tests for the Terraform infrastructure using [Terratest](https://terratest.gruntwork.io/).

## Test Coverage

The test suite covers all five required cases:

1. **VPC Tests** (`vpc_test.go`)
   - Verifies VPC is created with correct CIDR block (10.0.0.0/16)
   - Validates public subnets configuration (2 subnets in different AZs)
   - Validates private subnets configuration (2 subnets in different AZs)
   - Checks security group creation and rules

2. **ECS Cluster Tests** (`ecs_cluster_test.go`)
   - Verifies ECS cluster is created and active
   - Validates ALB configuration and availability zones
   - Checks ALB listener setup (port 80, HTTP)
   - Confirms default action (404 fixed-response)

3. **ECS Services Tests** (`ecs_service_test.go`)
   - Tests all four services: user-service, contest-service, voting-service, results-service
   - Validates task definitions (CPU, memory, network mode)
   - Checks container configurations (image, ports, essential flag)
   - Verifies environment variables for each service
   - Validates load balancer configuration and target groups
   - Confirms listener rules and path-based routing

4. **RDS Tests** (`rds_test.go`)
   - Validates RDS PostgreSQL instance configuration
   - Checks engine version, instance class, and storage
   - Verifies database is not publicly accessible
   - Validates DB subnet group and subnets
   - Tests security group rules (port 5432, VPC CIDR)
   - Confirms instance availability and endpoint

5. **MSK and Redis Tests** (`msk_redis_test.go`)
   - **MSK**: Validates Kafka cluster configuration (version 3.6.0, 2 brokers)
   - **MSK**: Checks broker node configuration and subnets
   - **MSK**: Verifies security group rules (port 9092)
   - **MSK**: Tests encryption settings
   - **Redis**: Validates ElastiCache replication group
   - **Redis**: Checks cache cluster configuration (cache.t3.micro, Redis 7.0)
   - **Redis**: Verifies subnet group and security group rules (port 6379)
   - **Redis**: Confirms endpoint availability

## Prerequisites

1. **Go**: Install Go 1.21 or later
   ```bash
   # macOS
   brew install go
   
   # Linux
   sudo apt-get install golang-go
   ```

2. **AWS Credentials**: Configure AWS credentials with appropriate permissions
   ```bash
   aws configure
   ```
   
   Required AWS permissions:
   - VPC (create/delete VPCs, subnets, security groups)
   - ECS (create/delete clusters, services, task definitions)
   - ELB (create/delete load balancers, target groups, listeners)
   - RDS (create/delete database instances, subnet groups)
   - MSK (create/delete Kafka clusters)
   - ElastiCache (create/delete Redis clusters)
   - IAM (create/delete roles and policies for ECS)

3. **Terraform**: Install Terraform
   ```bash
   # macOS
   brew install terraform
   
   # Linux
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   ```

## Setup

1. Navigate to the test directory:
   ```bash
   cd infra/test
   ```

2. Download Go dependencies:
   ```bash
   go mod download
   ```

3. Initialize Go modules (if needed):
   ```bash
   go mod tidy
   ```

## Running Tests

### Run All Tests
```bash
go test -v -timeout 90m
```

Note: Tests take 60-90 minutes to run because they create real AWS infrastructure.

### Run Specific Test
```bash
# VPC tests only
go test -v -timeout 30m -run TestVPCModule

# ECS cluster tests only
go test -v -timeout 30m -run TestECSClusterModule

# ECS services tests only
go test -v -timeout 60m -run TestECSServiceModule

# RDS tests only
go test -v -timeout 30m -run TestRDSModule

# MSK tests only
go test -v -timeout 45m -run TestMSKModule

# Redis tests only
go test -v -timeout 30m -run TestRedisModule
```

### Run Tests in Parallel
```bash
# Run with parallelism (default is 1)
go test -v -timeout 90m -parallel 3
```

## Test Architecture

Each test follows this pattern:

1. **Setup**: Creates VPC and prerequisite infrastructure
2. **Apply**: Runs `terraform init` and `terraform apply`
3. **Validate**: Uses AWS SDK to verify resource configuration
4. **Cleanup**: Runs `terraform destroy` (deferred at test start)

Tests use Terratest's retry logic to handle eventual consistency in AWS.

## Important Notes

### Cost Considerations
- Tests create real AWS resources that incur costs
- Most resources are minimal (t3.micro, t3.small) to keep costs low
- All resources are destroyed after tests complete
- Estimated cost per full test run: $5-10 (if tests complete quickly)

### Test Isolation
- Each test uses a unique `project_name` to avoid conflicts
- Tests can run in parallel without interference
- Resources are tagged with test identifiers

### Cleanup
If tests fail and resources aren't destroyed:
```bash
# Find resources by project name
aws resourcegroupstaggingapi get-resources \
  --tag-filters Key=Name,Values=test-*

# Or use AWS Console to manually delete resources
```

## Troubleshooting

### Timeout Errors
If tests timeout, increase the timeout value:
```bash
go test -v -timeout 120m
```

### AWS Rate Limiting
If you encounter rate limiting errors:
- Run tests sequentially instead of in parallel
- Add delays between test runs
- Request AWS service quota increases

### Terraform State Errors
If you see state locking errors:
- Wait for previous test to complete
- Manually remove lock files if necessary

### Test Failures
Common causes:
- Insufficient AWS permissions
- AWS service limits reached
- Networking issues
- Incorrect Terraform configuration

Check test output for specific error messages and stack traces.

## CI/CD Integration

To integrate tests into CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
name: Terraform Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-go@v4
        with:
          go-version: '1.21'
      - uses: hashicorp/setup-terraform@v2
      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      - name: Run tests
        run: |
          cd infra/test
          go test -v -timeout 90m
```

## Additional Resources

- [Terratest Documentation](https://terratest.gruntwork.io/)
- [AWS Go SDK Documentation](https://docs.aws.amazon.com/sdk-for-go/api/)
- [Terraform Testing Best Practices](https://www.terraform.io/docs/cloud/run/testing.html)
