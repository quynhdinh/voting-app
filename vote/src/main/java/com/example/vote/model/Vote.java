package com.example.vote.model;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
@Document
@AllArgsConstructor
@Data
public class Vote {
	@Id
	private String id;
	private String contestId;
	private String voterId;
	private String candidateIds;
	private Long createdAt;
}