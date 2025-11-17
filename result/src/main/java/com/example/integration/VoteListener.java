package com.example.integration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import com.example.service.ResultService;

@Component
@AllArgsConstructor
@KafkaListener(topics = "votes", groupId = "gid")
public class VoteListener {
	private final ResultService resultService;

	@KafkaHandler
	public void handleVote() {

	}
}