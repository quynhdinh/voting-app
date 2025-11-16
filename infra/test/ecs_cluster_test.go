package test

import (
	"testing"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/ecs"
	"github.com/aws/aws-sdk-go/service/elbv2"
	awsSDK "github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestECSClusterModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"

	// First, create VPC resources needed for ECS cluster
	vpcTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-ecs-cluster",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, vpcTerraformOptions)
	terraform.InitAndApply(t, vpcTerraformOptions)

	vpcId := terraform.Output(t, vpcTerraformOptions, "vpc_id")
	publicSubnets := terraform.OutputList(t, vpcTerraformOptions, "public_subnets")

	// Now create ECS cluster
	ecsClusterOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/ecs-cluster",
		Vars: map[string]interface{}{
			"project_name": "test-ecs-cluster",
			"vpc_id":       vpcId,
			"subnets":      publicSubnets,
		},
	})

	defer terraform.Destroy(t, ecsClusterOptions)
	terraform.InitAndApply(t, ecsClusterOptions)

	// Test 1: Verify ECS cluster is created
	clusterArn := terraform.Output(t, ecsClusterOptions, "cluster_arn")
	assert.NotEmpty(t, clusterArn, "ECS cluster ARN should not be empty")

	// Verify cluster exists and is active
	ecsClient := awsSDK.NewEcsClient(t, awsRegion)
	describeClusterInput := &ecs.DescribeClustersInput{
		Clusters: []*string{aws.String(clusterArn)},
	}
	describeClusterOutput, err := ecsClient.DescribeClusters(describeClusterInput)
	require.NoError(t, err, "Should be able to describe ECS cluster")
	require.Len(t, describeClusterOutput.Clusters, 1, "Should find one cluster")

	cluster := describeClusterOutput.Clusters[0]
	assert.Equal(t, "ACTIVE", *cluster.Status, "Cluster should be ACTIVE")
	assert.Contains(t, *cluster.ClusterName, "test-ecs-cluster", "Cluster name should contain project name")

	// Test 2: Verify ALB is created
	albArn := terraform.Output(t, ecsClusterOptions, "alb_arn")
	assert.NotEmpty(t, albArn, "ALB ARN should not be empty")

	// Verify ALB configuration
	elbClient := awsSDK.NewElbv2Client(t, awsRegion)
	describeALBInput := &elbv2.DescribeLoadBalancersInput{
		LoadBalancerArns: []*string{aws.String(albArn)},
	}
	describeALBOutput, err := elbClient.DescribeLoadBalancers(describeALBInput)
	require.NoError(t, err, "Should be able to describe ALB")
	require.Len(t, describeALBOutput.LoadBalancers, 1, "Should find one load balancer")

	alb := describeALBOutput.LoadBalancers[0]
	assert.Equal(t, "application", *alb.Type, "Load balancer should be of type 'application'")
	assert.Equal(t, "active", *alb.State.Code, "ALB should be active")
	assert.Equal(t, vpcId, *alb.VpcId, "ALB should be in the correct VPC")

	// Verify ALB is in the correct subnets
	assert.Len(t, alb.AvailabilityZones, 2, "ALB should be in 2 availability zones")
	subnetIds := make([]string, 0)
	for _, az := range alb.AvailabilityZones {
		subnetIds = append(subnetIds, *az.SubnetId)
	}
	for _, expectedSubnet := range publicSubnets {
		assert.Contains(t, subnetIds, expectedSubnet, "ALB should be in the expected subnet")
	}

	// Test 3: Verify ALB listener is created
	listenerArn := terraform.Output(t, ecsClusterOptions, "alb_listener_arn")
	assert.NotEmpty(t, listenerArn, "Listener ARN should not be empty")

	// Verify listener configuration
	describeListenerInput := &elbv2.DescribeListenersInput{
		ListenerArns: []*string{aws.String(listenerArn)},
	}
	describeListenerOutput, err := elbClient.DescribeListeners(describeListenerInput)
	require.NoError(t, err, "Should be able to describe listener")
	require.Len(t, describeListenerOutput.Listeners, 1, "Should find one listener")

	listener := describeListenerOutput.Listeners[0]
	assert.Equal(t, int64(80), *listener.Port, "Listener should be on port 80")
	assert.Equal(t, "HTTP", *listener.Protocol, "Listener protocol should be HTTP")
	assert.Equal(t, albArn, *listener.LoadBalancerArn, "Listener should belong to the ALB")

	// Test 4: Verify default action is fixed-response
	require.Len(t, listener.DefaultActions, 1, "Listener should have one default action")
	defaultAction := listener.DefaultActions[0]
	assert.Equal(t, "fixed-response", *defaultAction.Type, "Default action should be fixed-response")
	assert.Equal(t, "404", *defaultAction.FixedResponseConfig.StatusCode, "Default response should be 404")
	assert.Equal(t, "text/plain", *defaultAction.FixedResponseConfig.ContentType, "Content type should be text/plain")
}
