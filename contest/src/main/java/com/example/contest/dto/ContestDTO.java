package com.example.contest.dto;
import com.example.contest.model.Candidate;
import com.example.contest.model.Contest;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
@AllArgsConstructor
@Data
public class ContestDTO {
    private Contest contest;
    private List<Candidate> candidates;
}
