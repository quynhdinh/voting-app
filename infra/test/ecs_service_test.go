package test

import (
	"encoding/json"
	"testing"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ecs"
	"github.com/aws/aws-sdk-go/service/elbv2"
	awsSDK "github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// ContainerDefinition represents the structure of an ECS container definition
type ContainerDefinition struct {
	Name         string                   `json:"name"`
	Image        string                   `json:"image"`
	Essential    bool                     `json:"essential"`
	PortMappings []map[string]interface{} `json:"portMappings"`
	Environment  []map[string]string      `json:"environment"`
}

func TestECSServiceModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"

	// Setup VPC
	vpcTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-ecs-service",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, vpcTerraformOptions)
	terraform.InitAndApply(t, vpcTerraformOptions)

	vpcId := terraform.Output(t, vpcTerraformOptions, "vpc_id")
	privateSubnets := terraform.OutputList(t, vpcTerraformOptions, "private_subnets")
	publicSubnets := terraform.OutputList(t, vpcTerraformOptions, "public_subnets")
	appSgId := terraform.Output(t, vpcTerraformOptions, "app_sg_id")

	// Setup ECS Cluster
	ecsClusterOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/ecs-cluster",
		Vars: map[string]interface{}{
			"project_name": "test-ecs-service",
			"vpc_id":       vpcId,
			"subnets":      publicSubnets,
		},
	})

	defer terraform.Destroy(t, ecsClusterOptions)
	terraform.InitAndApply(t, ecsClusterOptions)

	clusterArn := terraform.Output(t, ecsClusterOptions, "cluster_arn")
	albListenerArn := terraform.Output(t, ecsClusterOptions, "alb_listener_arn")

	// Test different service configurations
	testCases := []struct {
		serviceName    string
		containerImage string
		containerPort  int
		desiredCount   int
		envVars        map[string]interface{}
	}{
		{
			serviceName:    "user-service",
			containerImage: "nginx:latest",
			containerPort:  8080,
			desiredCount:   2,
			envVars:        map[string]interface{}{},
		},
		{
			serviceName:    "contest-service",
			containerImage: "nginx:latest",
			containerPort:  8080,
			desiredCount:   2,
			envVars:        map[string]interface{}{},
		},
		{
			serviceName:    "voting-service",
			containerImage: "nginx:latest",
			containerPort:  8080,
			desiredCount:   3,
			envVars: map[string]interface{}{
				"KAFKA_BOOTSTRAP_SERVERS": "kafka.example.com:9092",
				"MONGO_URI":               "mongodb://localhost:27017/voting",
			},
		},
		{
			serviceName:    "results-service",
			containerImage: "nginx:latest",
			containerPort:  8080,
			desiredCount:   2,
			envVars: map[string]interface{}{
				"KAFKA_BOOTSTRAP_SERVERS": "kafka.example.com:9092",
				"REDIS_HOST":              "redis.example.com",
				"REDIS_PORT":              "6379",
				"RESULTS_DB_HOST":         "postgres.example.com",
				"RESULTS_DB_PORT":         "5432",
				"RESULTS_DB_NAME":         "results",
				"RESULTS_DB_USER":         "admin",
			},
		},
	}

	for _, tc := range testCases {
		tc := tc // capture range variable
		t.Run(tc.serviceName, func(t *testing.T) {
			// Create ECS service
			ecsServiceOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
				TerraformDir: "../modules/ecs-service",
				Vars: map[string]interface{}{
					"project_name":     "test-ecs-service",
					"service_name":     tc.serviceName,
					"cluster_arn":      clusterArn,
					"alb_listener_arn": albListenerArn,
					"subnets":          privateSubnets,
					"security_groups":  []string{appSgId},
					"container_image":  tc.containerImage,
					"container_port":   tc.containerPort,
					"desired_count":    tc.desiredCount,
					"env_vars":         tc.envVars,
				},
			})

			defer terraform.Destroy(t, ecsServiceOptions)
			terraform.InitAndApply(t, ecsServiceOptions)

			// Test 1: Verify ECS service is created
			ecsClient := awsSDK.NewEcsClient(t, awsRegion)
			serviceName := "test-ecs-service-" + tc.serviceName

			listServicesInput := &ecs.ListServicesInput{
				Cluster: aws.String(clusterArn),
			}
			listServicesOutput, err := ecsClient.ListServices(listServicesInput)
			require.NoError(t, err, "Should list services")

			// Find our service in the list
			var serviceArn *string
			for _, arn := range listServicesOutput.ServiceArns {
				if contains(*arn, serviceName) {
					serviceArn = arn
					break
				}
			}
			require.NotNil(t, serviceArn, "Service should exist")

			// Describe the service
			describeServicesInput := &ecs.DescribeServicesInput{
				Cluster:  aws.String(clusterArn),
				Services: []*string{serviceArn},
			}
			describeServicesOutput, err := ecsClient.DescribeServices(describeServicesInput)
			require.NoError(t, err, "Should describe service")
			require.Len(t, describeServicesOutput.Services, 1, "Should find one service")

			service := describeServicesOutput.Services[0]
			assert.Equal(t, int64(tc.desiredCount), *service.DesiredCount, "Desired count should match")
			assert.Equal(t, "FARGATE", *service.LaunchType, "Launch type should be FARGATE")

			// Test 2: Verify task definition
			taskDefArn := service.TaskDefinition
			require.NotNil(t, taskDefArn, "Task definition ARN should not be nil")

			describeTaskDefInput := &ecs.DescribeTaskDefinitionInput{
				TaskDefinition: taskDefArn,
			}
			describeTaskDefOutput, err := ecsClient.DescribeTaskDefinition(describeTaskDefInput)
			require.NoError(t, err, "Should describe task definition")

			taskDef := describeTaskDefOutput.TaskDefinition
			assert.Equal(t, "256", *taskDef.Cpu, "CPU should be 256")
			assert.Equal(t, "512", *taskDef.Memory, "Memory should be 512")
			assert.Equal(t, "awsvpc", *taskDef.NetworkMode, "Network mode should be awsvpc")
			assert.Contains(t, *taskDef.Family, tc.serviceName, "Task family should contain service name")

			// Test 3: Verify container definition
			require.Len(t, taskDef.ContainerDefinitions, 1, "Should have one container definition")
			container := taskDef.ContainerDefinitions[0]
			assert.Equal(t, tc.serviceName, *container.Name, "Container name should match service name")
			assert.Equal(t, tc.containerImage, *container.Image, "Container image should match")
			assert.True(t, *container.Essential, "Container should be essential")

			// Verify port mappings
			require.Len(t, container.PortMappings, 1, "Should have one port mapping")
			portMapping := container.PortMappings[0]
			assert.Equal(t, int64(tc.containerPort), *portMapping.ContainerPort, "Container port should match")
			assert.Equal(t, "tcp", *portMapping.Protocol, "Protocol should be tcp")

			// Test 4: Verify environment variables
			if len(tc.envVars) > 0 {
				assert.Len(t, container.Environment, len(tc.envVars), "Should have correct number of env vars")

				envMap := make(map[string]string)
				for _, env := range container.Environment {
					envMap[*env.Name] = *env.Value
				}

				for key, value := range tc.envVars {
					assert.Equal(t, value, envMap[key], "Environment variable %s should match", key)
				}
			}

			// Test 5: Verify load balancer configuration
			require.Len(t, service.LoadBalancers, 1, "Service should have one load balancer")
			lb := service.LoadBalancers[0]
			assert.Equal(t, tc.serviceName, *lb.ContainerName, "Load balancer container name should match")
			assert.Equal(t, int64(tc.containerPort), *lb.ContainerPort, "Load balancer container port should match")
			assert.NotEmpty(t, *lb.TargetGroupArn, "Target group ARN should not be empty")

			// Test 6: Verify target group
			elbClient := awsSDK.NewElbv2Client(t, awsRegion)
			describeTGInput := &elbv2.DescribeTargetGroupsInput{
				TargetGroupArns: []*string{lb.TargetGroupArn},
			}
			describeTGOutput, err := elbClient.DescribeTargetGroups(describeTGInput)
			require.NoError(t, err, "Should describe target group")
			require.Len(t, describeTGOutput.TargetGroups, 1, "Should find one target group")

			targetGroup := describeTGOutput.TargetGroups[0]
			assert.Equal(t, "HTTP", *targetGroup.Protocol, "Target group protocol should be HTTP")
			assert.Equal(t, int64(tc.containerPort), *targetGroup.Port, "Target group port should match")
			assert.Equal(t, "ip", *targetGroup.TargetType, "Target type should be ip")
			assert.Equal(t, vpcId, *targetGroup.VpcId, "Target group should be in correct VPC")

			// Test 7: Verify listener rule exists
			describeRulesInput := &elbv2.DescribeRulesInput{
				ListenerArn: aws.String(albListenerArn),
			}
			describeRulesOutput, err := elbClient.DescribeRules(describeRulesInput)
			require.NoError(t, err, "Should describe rules")

			// Find rule for this service
			var serviceRule *elbv2.Rule
			for _, rule := range describeRulesOutput.Rules {
				if rule.Actions != nil && len(rule.Actions) > 0 {
					if rule.Actions[0].TargetGroupArn != nil && *rule.Actions[0].TargetGroupArn == *lb.TargetGroupArn {
						serviceRule = rule
						break
					}
				}
			}
			require.NotNil(t, serviceRule, "Should find listener rule for service")

			// Verify rule conditions
			require.Len(t, serviceRule.Conditions, 1, "Rule should have one condition")
			condition := serviceRule.Conditions[0]
			assert.Equal(t, "path-pattern", *condition.Field, "Condition should be path-pattern")
			require.Len(t, condition.Values, 1, "Should have one path value")
			assert.Equal(t, "/"+tc.serviceName+"/*", *condition.Values[0], "Path should match service name")
		})
	}
}

func contains(s, substr string) bool {
	return len(s) >= len(substr) && (s == substr || len(s) > len(substr) && (s[len(s)-len(substr):] == substr || s[:len(substr)] == substr || containsSubstring(s, substr)))
}

func containsSubstring(s, substr string) bool {
	for i := 0; i <= len(s)-len(substr); i++ {
		if s[i:i+len(substr)] == substr {
			return true
		}
	}
	return false
}
