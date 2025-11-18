package com.example.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCandidateDTO {
    private String name;
    private String description;
}
