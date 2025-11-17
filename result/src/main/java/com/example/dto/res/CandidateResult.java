package com.example.dto.res;
public record CandidateResult(
        Long candidateId, 
        String name, 
        String description, 
        Long totalVotes) {}