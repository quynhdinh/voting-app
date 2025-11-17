package com.example.service;
import com.example.repository.ResultRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResultService {
    private final ResultRepository resultRepository;
    
    // increment the vote in redis
    // increment the vote in postgres
    public void processVote() {
    }
    public void processUnvote() {
    }
}
