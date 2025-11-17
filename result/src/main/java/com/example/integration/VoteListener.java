package com.example.integration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;

import com.example.dto.VoteDTO;
import com.example.service.ResultService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class VoteListener {
	private final ResultService resultService;
	@KafkaListener(topics = "votes", groupId = "gid")
	public void handleVote(String message) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		VoteDTO vote = mapper.readValue(message, VoteDTO.class);
		System.out.println("Received vote message: " + vote);
		resultService.processVote(vote);
	}
}