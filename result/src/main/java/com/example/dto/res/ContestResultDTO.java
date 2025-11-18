package com.example.dto.res;

import java.util.List;

public record ContestResultDTO(
    Long contestId,
    String title,
    String description,
    List<CandidateResult> candidateResults
) {}
