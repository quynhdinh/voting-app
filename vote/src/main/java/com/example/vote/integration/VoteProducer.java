package com.example.vote.integration;
import com.example.vote.model.Vote;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class VoteProducer {
	private KafkaTemplate<String, String> kafkaTemplate;

	public void sendVote(Vote vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		// send with different key to distribute across partitions
		// now not one consumer will get all messages
		kafkaTemplate.send("votes", String.valueOf(vote.getId()), writeValueAsString);
	}
	public void sendUnvote(Vote vote) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String writeValueAsString = objectMapper.writeValueAsString(vote);
		// send with different key to distribute across partitions
		// now not one consumer will get all messages
		kafkaTemplate.send("unvotes", String.valueOf(vote.getId()), writeValueAsString);
	}
}
