package com.example.contest.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "contests")
@Data
public class Contest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private Long candidateId;
	private String description;
	private Long startTime;
	private Long endTime;
	private Long createdBy;
	private Long createdAt;
}