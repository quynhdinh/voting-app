package com.example.vote.model;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User_Vote_State {
	@Id
	private String id;
	private Long voterId;
	private Long contestId;
	private String candidateIds;
}
