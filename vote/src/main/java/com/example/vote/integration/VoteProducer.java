package com.example.vote.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.example.vote.dto.VoteDTO;
@Component
@AllArgsConstructor
@Slf4j
public class VoteProducer {
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendVote(VoteDTO vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		kafkaTemplate.send("votes", String.valueOf(vote.contestId()), writeValueAsString);
	}
	public void sendUnvote(VoteDTO vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		// send with different key to distribute across partitions
		// now not one consumer will get all messages
		kafkaTemplate.send("unvotes", String.valueOf(vote.contestId()), writeValueAsString);
	}
}
