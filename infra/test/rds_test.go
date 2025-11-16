package test

import (
	"testing"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/rds"
	awsSDK "github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestRDSModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"
	dbUsername := "testuser"
	dbPassword := "TestPassword123!"
	dbName := "testdb"

	// Setup VPC
	vpcTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-rds",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, vpcTerraformOptions)
	terraform.InitAndApply(t, vpcTerraformOptions)

	vpcId := terraform.Output(t, vpcTerraformOptions, "vpc_id")
	privateSubnets := terraform.OutputList(t, vpcTerraformOptions, "private_subnets")

	// Create RDS instance
	rdsTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/rds",
		Vars: map[string]interface{}{
			"project_name": "test-rds",
			"vpc_id":       vpcId,
			"subnets":      privateSubnets,
			"db_username":  dbUsername,
			"db_password":  dbPassword,
			"db_name":      dbName,
		},
	})

	defer terraform.Destroy(t, rdsTerraformOptions)
	terraform.InitAndApply(t, rdsTerraformOptions)

	// Test 1: Verify RDS instance outputs
	dbEndpoint := terraform.Output(t, rdsTerraformOptions, "db_endpoint")
	dbPort := terraform.Output(t, rdsTerraformOptions, "db_port")
	outputDbName := terraform.Output(t, rdsTerraformOptions, "db_name")
	outputDbUsername := terraform.Output(t, rdsTerraformOptions, "db_username")

	assert.NotEmpty(t, dbEndpoint, "DB endpoint should not be empty")
	assert.Equal(t, "5432", dbPort, "DB port should be 5432")
	assert.Equal(t, dbName, outputDbName, "DB name should match")
	assert.Equal(t, dbUsername, outputDbUsername, "DB username should match")

	// Test 2: Verify RDS instance configuration
	rdsClient := awsSDK.NewRdsClient(t, awsRegion)
	
	dbInstanceId := "test-rds-pg"
	describeDBInput := &rds.DescribeDBInstancesInput{
		DBInstanceIdentifier: aws.String(dbInstanceId),
	}
	describeDBOutput, err := rdsClient.DescribeDBInstances(describeDBInput)
	require.NoError(t, err, "Should be able to describe RDS instance")
	require.Len(t, describeDBOutput.DBInstances, 1, "Should find one DB instance")

	dbInstance := describeDBOutput.DBInstances[0]

	// Verify basic configuration
	assert.Equal(t, "postgres", *dbInstance.Engine, "Engine should be postgres")
	assert.Equal(t, "15.3", *dbInstance.EngineVersion, "Engine version should be 15.3")
	assert.Equal(t, "db.t3.micro", *dbInstance.DBInstanceClass, "Instance class should be db.t3.micro")
	assert.Equal(t, int64(20), *dbInstance.AllocatedStorage, "Allocated storage should be 20 GB")
	assert.Equal(t, dbName, *dbInstance.DBName, "Database name should match")
	assert.Equal(t, dbUsername, *dbInstance.MasterUsername, "Master username should match")

	// Test 3: Verify RDS instance is not publicly accessible
	assert.False(t, *dbInstance.PubliclyAccessible, "RDS instance should not be publicly accessible")

	// Test 4: Verify multi-AZ configuration
	assert.False(t, *dbInstance.MultiAZ, "Multi-AZ should be disabled for test")

	// Test 5: Verify DB subnet group
	require.NotNil(t, dbInstance.DBSubnetGroup, "DB subnet group should not be nil")
	subnetGroup := dbInstance.DBSubnetGroup
	assert.Equal(t, "test-rds-rds-subnets", *subnetGroup.DBSubnetGroupName, "Subnet group name should match")
	assert.Equal(t, vpcId, *subnetGroup.VpcId, "Subnet group should be in correct VPC")

	// Verify subnets in subnet group
	assert.Len(t, subnetGroup.Subnets, 2, "Subnet group should have 2 subnets")
	subnetIds := make([]string, 0)
	for _, subnet := range subnetGroup.Subnets {
		subnetIds = append(subnetIds, *subnet.SubnetIdentifier)
	}
	for _, expectedSubnet := range privateSubnets {
		assert.Contains(t, subnetIds, expectedSubnet, "Subnet group should contain expected subnet")
	}

	// Test 6: Verify security group configuration
	require.Len(t, dbInstance.VpcSecurityGroups, 1, "Should have one VPC security group")
	sgId := *dbInstance.VpcSecurityGroups[0].VpcSecurityGroupId

	securityGroup := awsSDK.GetSecurityGroupById(t, sgId, awsRegion)
	assert.Equal(t, vpcId, securityGroup.VpcId, "Security group should be in correct VPC")
	assert.Equal(t, "test-rds-rds-sg", securityGroup.Name, "Security group name should match")

	// Test 7: Verify security group ingress rules
	require.Len(t, securityGroup.IngressRules, 1, "Should have one ingress rule")
	ingressRule := securityGroup.IngressRules[0]
	assert.Equal(t, int64(5432), ingressRule.FromPort, "Ingress from port should be 5432")
	assert.Equal(t, int64(5432), ingressRule.ToPort, "Ingress to port should be 5432")
	assert.Equal(t, "tcp", ingressRule.Protocol, "Ingress protocol should be tcp")
	
	// Verify ingress allows VPC CIDR
	found := false
	for _, cidr := range ingressRule.CidrBlocks {
		if cidr == "10.0.0.0/16" {
			found = true
			break
		}
	}
	assert.True(t, found, "Ingress should allow VPC CIDR 10.0.0.0/16")

	// Test 8: Verify security group egress rules
	require.Len(t, securityGroup.EgressRules, 1, "Should have one egress rule")
	egressRule := securityGroup.EgressRules[0]
	assert.Equal(t, int64(0), egressRule.FromPort, "Egress from port should be 0")
	assert.Equal(t, int64(0), egressRule.ToPort, "Egress to port should be 0")
	assert.Equal(t, "-1", egressRule.Protocol, "Egress protocol should be -1 (all)")

	// Test 9: Verify instance status
	assert.Equal(t, "available", *dbInstance.DBInstanceStatus, "DB instance should be available")

	// Test 10: Verify endpoint accessibility
	assert.NotNil(t, dbInstance.Endpoint, "DB endpoint should not be nil")
	assert.Equal(t, dbEndpoint, *dbInstance.Endpoint.Address, "Endpoint address should match output")
	assert.Equal(t, int64(5432), *dbInstance.Endpoint.Port, "Endpoint port should be 5432")
}
