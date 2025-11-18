package com.example.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteDTO {
	private Long contestId;
	private Long voterId;
	private Long candidateId;
}
// a sample body to send vote request
// {
//   "contestId": 1,
//   "voterId": 1,
//   "candidateId": 2
// }
