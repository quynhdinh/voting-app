package test

import (
	"testing"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/elasticache"
	"github.com/aws/aws-sdk-go/service/kafka"
	awsSDK "github.com/gruntwork-io/terratest/modules/aws"
	"github.com/gruntwork-io/terratest/modules/terraform"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestMSKModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"

	// Setup VPC
	vpcTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-msk",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, vpcTerraformOptions)
	terraform.InitAndApply(t, vpcTerraformOptions)

	vpcId := terraform.Output(t, vpcTerraformOptions, "vpc_id")
	privateSubnets := terraform.OutputList(t, vpcTerraformOptions, "private_subnets")

	// Create MSK cluster
	mskTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/msk",
		Vars: map[string]interface{}{
			"project_name": "test-msk",
			"vpc_id":       vpcId,
			"subnets":      privateSubnets,
		},
	})

	defer terraform.Destroy(t, mskTerraformOptions)
	terraform.InitAndApply(t, mskTerraformOptions)

	// Test 1: Verify MSK cluster outputs
	bootstrapBrokers := terraform.Output(t, mskTerraformOptions, "bootstrap_brokers")
	assert.NotEmpty(t, bootstrapBrokers, "Bootstrap brokers should not be empty")

	// Test 2: Verify MSK cluster configuration
	kafkaClient := kafka.New(awsSDK.NewAuthenticatedSession(t, awsRegion))

	clusterName := "test-msk-msk"
	listClustersInput := &kafka.ListClustersV2Input{}
	listClustersOutput, err := kafkaClient.ListClustersV2(listClustersInput)
	require.NoError(t, err, "Should be able to list MSK clusters")

	// Find our cluster
	var clusterArn *string
	for _, cluster := range listClustersOutput.ClusterInfoList {
		if cluster.ClusterName != nil && *cluster.ClusterName == clusterName {
			clusterArn = cluster.ClusterArn
			break
		}
	}
	require.NotNil(t, clusterArn, "Should find MSK cluster")

	// Get detailed cluster info
	describeClusterInput := &kafka.DescribeClusterV2Input{
		ClusterArn: clusterArn,
	}
	describeClusterOutput, err := kafkaClient.DescribeClusterV2(describeClusterInput)
	require.NoError(t, err, "Should be able to describe MSK cluster")

	clusterInfo := describeClusterOutput.ClusterInfo
	assert.Equal(t, "3.6.0", *clusterInfo.Provisioned.KafkaVersion, "Kafka version should be 3.6.0")
	assert.Equal(t, int64(2), *clusterInfo.Provisioned.NumberOfBrokerNodes, "Should have 2 broker nodes")

	// Test 3: Verify broker node configuration
	brokerNodeInfo := clusterInfo.Provisioned.BrokerNodeGroupInfo
	assert.Equal(t, "kafka.t3.small", *brokerNodeInfo.InstanceType, "Instance type should be kafka.t3.small")
	assert.Len(t, brokerNodeInfo.ClientSubnets, 2, "Should have 2 client subnets")

	// Verify subnets match
	for _, expectedSubnet := range privateSubnets {
		found := false
		for _, subnet := range brokerNodeInfo.ClientSubnets {
			if *subnet == expectedSubnet {
				found = true
				break
			}
		}
		assert.True(t, found, "Broker should be in expected subnet %s", expectedSubnet)
	}

	// Test 4: Verify security group configuration
	require.Len(t, brokerNodeInfo.SecurityGroups, 1, "Should have one security group")
	sgId := *brokerNodeInfo.SecurityGroups[0]

	securityGroup := awsSDK.GetSecurityGroupById(t, sgId, awsRegion)
	assert.Equal(t, vpcId, securityGroup.VpcId, "Security group should be in correct VPC")
	assert.Equal(t, "test-msk-msk-sg", securityGroup.Name, "Security group name should match")

	// Test 5: Verify security group rules
	require.Len(t, securityGroup.IngressRules, 1, "Should have one ingress rule")
	ingressRule := securityGroup.IngressRules[0]
	assert.Equal(t, int64(9092), ingressRule.FromPort, "Ingress from port should be 9092")
	assert.Equal(t, int64(9092), ingressRule.ToPort, "Ingress to port should be 9092")
	assert.Equal(t, "tcp", ingressRule.Protocol, "Ingress protocol should be tcp")

	// Test 6: Verify encryption configuration
	encryptionInfo := clusterInfo.Provisioned.EncryptionInfo
	assert.NotNil(t, encryptionInfo, "Encryption info should not be nil")
	assert.Equal(t, "PLAINTEXT", *encryptionInfo.EncryptionInTransit.ClientBroker, "Client-broker encryption should be PLAINTEXT")
	assert.True(t, *encryptionInfo.EncryptionInTransit.InCluster, "In-cluster encryption should be enabled")

	// Test 7: Verify cluster state
	assert.Equal(t, "ACTIVE", *clusterInfo.State, "Cluster state should be ACTIVE")
}

func TestRedisModule(t *testing.T) {
	t.Parallel()

	awsRegion := "us-east-1"

	// Setup VPC
	vpcTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/vpc",
		Vars: map[string]interface{}{
			"project_name": "test-redis",
			"aws_region":   awsRegion,
		},
	})

	defer terraform.Destroy(t, vpcTerraformOptions)
	terraform.InitAndApply(t, vpcTerraformOptions)

	vpcId := terraform.Output(t, vpcTerraformOptions, "vpc_id")
	privateSubnets := terraform.OutputList(t, vpcTerraformOptions, "private_subnets")

	// Create Redis cluster
	redisTerraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../modules/redis",
		Vars: map[string]interface{}{
			"project_name": "test-redis",
			"vpc_id":       vpcId,
			"subnets":      privateSubnets,
		},
	})

	defer terraform.Destroy(t, redisTerraformOptions)
	terraform.InitAndApply(t, redisTerraformOptions)

	// Test 1: Verify Redis outputs
	redisPrimaryEndpoint := terraform.Output(t, redisTerraformOptions, "redis_primary_endpoint")
	redisPort := terraform.Output(t, redisTerraformOptions, "redis_port")

	assert.NotEmpty(t, redisPrimaryEndpoint, "Redis primary endpoint should not be empty")
	assert.Equal(t, "6379", redisPort, "Redis port should be 6379")

	// Test 2: Verify Redis replication group configuration
	elasticacheClient := elasticache.New(awsSDK.NewAuthenticatedSession(t, awsRegion))

	replicationGroupId := "test-redis-redis-rg"
	describeRGInput := &elasticache.DescribeReplicationGroupsInput{
		ReplicationGroupId: aws.String(replicationGroupId),
	}
	describeRGOutput, err := elasticacheClient.DescribeReplicationGroups(describeRGInput)
	require.NoError(t, err, "Should be able to describe replication group")
	require.Len(t, describeRGOutput.ReplicationGroups, 1, "Should find one replication group")

	replicationGroup := describeRGOutput.ReplicationGroups[0]

	// Verify basic configuration
	assert.Equal(t, "redis", *replicationGroup.Description, "Description should match")
	assert.Equal(t, "available", *replicationGroup.Status, "Status should be available")
	assert.False(t, *replicationGroup.AutomaticFailover != "disabled", "Automatic failover should be disabled")
	assert.False(t, *replicationGroup.TransitEncryptionEnabled, "Transit encryption should be disabled")
	assert.False(t, *replicationGroup.AtRestEncryptionEnabled, "At-rest encryption should be disabled")

	// Test 3: Verify cache cluster configuration
	require.Len(t, replicationGroup.MemberClusters, 1, "Should have one member cluster")
	cacheClusterId := *replicationGroup.MemberClusters[0]

	describeCacheInput := &elasticache.DescribeCacheClustersInput{
		CacheClusterId:    aws.String(cacheClusterId),
		ShowCacheNodeInfo: aws.Bool(true),
	}
	describeCacheOutput, err := elasticacheClient.DescribeCacheClusters(describeCacheInput)
	require.NoError(t, err, "Should be able to describe cache cluster")
	require.Len(t, describeCacheOutput.CacheClusters, 1, "Should find one cache cluster")

	cacheCluster := describeCacheOutput.CacheClusters[0]
	assert.Equal(t, "cache.t3.micro", *cacheCluster.CacheNodeType, "Node type should be cache.t3.micro")
	assert.Equal(t, "redis", *cacheCluster.Engine, "Engine should be redis")
	assert.Equal(t, "7.0", *cacheCluster.EngineVersion, "Engine version should be 7.0")

	// Test 4: Verify subnet group
	require.NotNil(t, cacheCluster.CacheSubnetGroupName, "Cache subnet group name should not be nil")
	subnetGroupName := *cacheCluster.CacheSubnetGroupName

	describeSubnetGroupInput := &elasticache.DescribeCacheSubnetGroupsInput{
		CacheSubnetGroupName: aws.String(subnetGroupName),
	}
	describeSubnetGroupOutput, err := elasticacheClient.DescribeCacheSubnetGroups(describeSubnetGroupInput)
	require.NoError(t, err, "Should be able to describe subnet group")
	require.Len(t, describeSubnetGroupOutput.CacheSubnetGroups, 1, "Should find one subnet group")

	subnetGroup := describeSubnetGroupOutput.CacheSubnetGroups[0]
	assert.Equal(t, "test-redis-redis-subnets", *subnetGroup.CacheSubnetGroupName, "Subnet group name should match")
	assert.Equal(t, vpcId, *subnetGroup.VpcId, "Subnet group should be in correct VPC")

	// Verify subnets
	assert.Len(t, subnetGroup.Subnets, 2, "Subnet group should have 2 subnets")
	subnetIds := make([]string, 0)
	for _, subnet := range subnetGroup.Subnets {
		subnetIds = append(subnetIds, *subnet.SubnetIdentifier)
	}
	for _, expectedSubnet := range privateSubnets {
		assert.Contains(t, subnetIds, expectedSubnet, "Subnet group should contain expected subnet")
	}

	// Test 5: Verify security group
	require.Len(t, cacheCluster.SecurityGroups, 1, "Should have one security group")
	sgId := *cacheCluster.SecurityGroups[0].SecurityGroupId

	securityGroup := awsSDK.GetSecurityGroupById(t, sgId, awsRegion)
	assert.Equal(t, vpcId, securityGroup.VpcId, "Security group should be in correct VPC")
	assert.Equal(t, "test-redis-redis-sg", securityGroup.Name, "Security group name should match")

	// Test 6: Verify security group ingress rules
	require.Len(t, securityGroup.IngressRules, 1, "Should have one ingress rule")
	ingressRule := securityGroup.IngressRules[0]
	assert.Equal(t, int64(6379), ingressRule.FromPort, "Ingress from port should be 6379")
	assert.Equal(t, int64(6379), ingressRule.ToPort, "Ingress to port should be 6379")
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

	// Test 7: Verify security group egress rules
	require.Len(t, securityGroup.EgressRules, 1, "Should have one egress rule")
	egressRule := securityGroup.EgressRules[0]
	assert.Equal(t, int64(0), egressRule.FromPort, "Egress from port should be 0")
	assert.Equal(t, int64(0), egressRule.ToPort, "Egress to port should be 0")
	assert.Equal(t, "-1", egressRule.Protocol, "Egress protocol should be -1 (all)")

	// Test 8: Verify endpoint
	assert.NotNil(t, replicationGroup.NodeGroups, "Node groups should not be nil")
	require.Len(t, replicationGroup.NodeGroups, 1, "Should have one node group")
	nodeGroup := replicationGroup.NodeGroups[0]
	assert.NotNil(t, nodeGroup.PrimaryEndpoint, "Primary endpoint should not be nil")
	assert.Equal(t, redisPrimaryEndpoint, *nodeGroup.PrimaryEndpoint.Address, "Primary endpoint should match output")
	assert.Equal(t, int64(6379), *nodeGroup.PrimaryEndpoint.Port, "Primary endpoint port should be 6379")
}
