package com.example.dto.req;
import java.util.List;

public record ContestDetailsDTO(ContestDTO contest, List<CandidateDTO> candidates){}
