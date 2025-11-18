package com.example.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContestDTO {
    private Long createdBy;
    private String title;
    private String description;
    private Long startTime;
    private Long endTime;
    @JsonProperty("candidates")  // <--- add this
    private List<CreateCandidateDTO> candidates;
}
