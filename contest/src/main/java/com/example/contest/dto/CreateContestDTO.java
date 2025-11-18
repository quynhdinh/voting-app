package com.example.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContestDTO {
    private Long createdBy;
    private String title;
    private String description;
    private Long startTime;
    private Long endTime;
    private List<CreateCandidateDTO> candidates;
}
