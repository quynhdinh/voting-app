package com.example.integration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.dto.VoteDTO;
import com.example.service.ResultService;

@Component
@AllArgsConstructor
public class UnvoteListener {
    private final ResultService resultService;
    @KafkaListener(topics = "unvotes", groupId = "gid")
    public void handleUnvote(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
		VoteDTO vote = mapper.readValue(message, VoteDTO.class);
		System.out.println("Received unvote message: " + vote);
        resultService.processUnvote(vote);
    }
    
}
