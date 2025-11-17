package com.example.integration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import com.example.service.ResultService;

@Component
@KafkaListener(topics = "unvotes", groupId = "gid")
@AllArgsConstructor
public class UnvoteListener {
    private final ResultService resultService;

    @KafkaHandler
    public void handleUnvote() {
        resultService.processUnvote();
    }
    
}
