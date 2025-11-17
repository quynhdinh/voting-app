package com.example.vote.integration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
@Configuration
public class Config {
	@Bean
	public NewTopic votesTopic() {
		return TopicBuilder.name("votes").partitions(1).replicas(1).build();
	}
	@Bean
	public NewTopic unvotesTopic() {
		return TopicBuilder.name("unvotes").partitions(1).replicas(1).build();
	}
}
