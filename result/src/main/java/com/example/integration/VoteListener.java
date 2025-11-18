package com.example.integration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.dto.VoteDTO;
import com.example.service.ResultService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
@Slf4j
public class VoteListener {
	private final ResultService resultService;
	@KafkaListener(topics = "votes", groupId = "gid")
	public void handleVote(String message) throws Exception {
		log.info("Received vote message: {}", message);
		ObjectMapper mapper = new ObjectMapper();
		VoteDTO vote = mapper.readValue(message, VoteDTO.class);
		log.info("Received vote message: {}", vote);
		resultService.processVote(vote);
	}
}