package com.example.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class ContestResult {
    private Long contestId;
    private List<CandidateResult> candidateResults;
}

record CandidateResult(Long candidateId, int totalVotes) {}