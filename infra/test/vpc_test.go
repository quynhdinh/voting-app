package test

import (
	"testing"

	"github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
)

func TestVPCModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"

	terraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-voting-system",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, terraformOptions)
	terraform.InitAndApply(t, terraformOptions)

	// Test 1: Verify VPC is created with correct CIDR block
	vpcId := terraform.Output(t, terraformOptions, "vpc_id")
	assert.NotEmpty(t, vpcId, "VPC ID should not be empty")

	vpc := aws.GetVpcById(t, vpcId, awsRegion)
	assert.Equal(t, "10.0.0.0/16", vpc.Cidr, "VPC CIDR should be 10.0.0.0/16")
	assert.True(t, vpc.EnableDnsSupport, "VPC should have DNS support enabled")
	assert.True(t, vpc.EnableDnsHostnames, "VPC should have DNS hostnames enabled")

	// Test 2: Verify public subnets are created correctly
	publicSubnets := terraform.OutputList(t, terraformOptions, "public_subnets")
	assert.Len(t, publicSubnets, 2, "Should have 2 public subnets")

	for i, subnetId := range publicSubnets {
		subnet := aws.GetSubnetById(t, subnetId, awsRegion)
		assert.Equal(t, vpcId, subnet.VpcId, "Subnet should belong to the created VPC")
		assert.True(t, subnet.MapPublicIpOnLaunch, "Public subnet should map public IP on launch")

		// Verify CIDR blocks
		expectedCIDR := ""
		if i == 0 {
			expectedCIDR = "10.0.1.0/24"
		} else {
			expectedCIDR = "10.0.2.0/24"
		}
		assert.Equal(t, expectedCIDR, subnet.Cidr, "Public subnet should have correct CIDR block")

		// Verify availability zones
		expectedAZ := awsRegion + string(rune('a'+i))
		assert.Equal(t, expectedAZ, subnet.AvailabilityZone, "Subnet should be in correct AZ")
	}

	// Test 3: Verify private subnets are created correctly
	privateSubnets := terraform.OutputList(t, terraformOptions, "private_subnets")
	assert.Len(t, privateSubnets, 2, "Should have 2 private subnets")

	for i, subnetId := range privateSubnets {
		subnet := aws.GetSubnetById(t, subnetId, awsRegion)
		assert.Equal(t, vpcId, subnet.VpcId, "Subnet should belong to the created VPC")
		assert.False(t, subnet.MapPublicIpOnLaunch, "Private subnet should not map public IP on launch")

		// Verify CIDR blocks
		expectedCIDR := ""
		if i == 0 {
			expectedCIDR = "10.0.11.0/24"
		} else {
			expectedCIDR = "10.0.12.0/24"
		}
		assert.Equal(t, expectedCIDR, subnet.Cidr, "Private subnet should have correct CIDR block")

		// Verify availability zones
		expectedAZ := awsRegion + string(rune('a'+i))
		assert.Equal(t, expectedAZ, subnet.AvailabilityZone, "Subnet should be in correct AZ")
	}

	// Test 4: Verify security group is created
	appSgId := terraform.Output(t, terraformOptions, "app_sg_id")
	assert.NotEmpty(t, appSgId, "App security group ID should not be empty")

	// Verify security group belongs to VPC
	securityGroup := aws.GetSecurityGroupById(t, appSgId, awsRegion)
	assert.Equal(t, vpcId, securityGroup.VpcId, "Security group should belong to the created VPC")

	// Test 5: Verify security group rules
	// Ingress rule: internal VPC traffic
	assert.Len(t, securityGroup.IngressRules, 1, "Should have 1 ingress rule")
	ingressRule := securityGroup.IngressRules[0]
	assert.Equal(t, int64(0), ingressRule.FromPort, "Ingress from port should be 0")
	assert.Equal(t, int64(0), ingressRule.ToPort, "Ingress to port should be 0")
	assert.Equal(t, "-1", ingressRule.Protocol, "Ingress protocol should be -1 (all)")

	// Egress rule: allow all outbound
	assert.Len(t, securityGroup.EgressRules, 1, "Should have 1 egress rule")
	egressRule := securityGroup.EgressRules[0]
	assert.Equal(t, int64(0), egressRule.FromPort, "Egress from port should be 0")
	assert.Equal(t, int64(0), egressRule.ToPort, "Egress to port should be 0")
	assert.Equal(t, "-1", egressRule.Protocol, "Egress protocol should be -1 (all)")
}
